(ns halti-builder.api.build
  (:require [clojure.core.async :as async :refer :all :exclude [map into reduce merge partition partition-by take]]
            [halti-builder.git :refer [update-repo]]
            [halti-builder.utils :refer [env from-json flip stream->channel]]
            [halti-builder.tar :refer [directory->tar]]
            [halti-builder.db.builds :as db]
            [halti-builder.events :as events]
            [halti-builder.pg :refer [with-pg-connection]]
            [schema.core :as s]
            [taoensso.timbre :as timbre :refer [spy info error]]
            [clojure.java.jdbc :as jdbc]
            [clj-time.coerce :refer [to-timestamp]]
            [clj-time.core :refer [now]]
            [halti-builder.docker :refer [build-image tag-image push-image ping]]))


(def has-keyword? (flip contains?))

(defn strip-control-chars [s]
  (.replaceAll s "[\u0000-\u001f]" ""))

(defn process-message [message]
  (condp has-keyword? message
    ; Errors
    :errorDetail (get-in message [:errorDetail :message])
    ; Normal push logs
    :status (str (:status message) ": " (:id message))
    :stream (:stream message)
    :message (:message message)
    ""))


(def insert-event (with-pg-connection db/insert-event))
(def update-build-status (with-pg-connection db/update-build-status))
(def update-build-commit-data (with-pg-connection db/update-build-commit-data))
(def complete-build (with-pg-connection db/complete-build))
;(def insert-event (with-pg-connection (fn [& args] (spy args))))

(defn- build->credentials [build]
  {:private (-> build :private_key)
   :public (-> build :public_key)})

(def Build
  {:id s/Str
   :author s/Str
   :branch s/Str
   :tag s/Str
   :source {:path s/Str
            :url s/Str
            :branch s/Str
            :name s/Str
            :private-key s/Str
            :public-key s/Str}
   :registry {:url s/Str}})

(defn latest-commit->commit-data [latest-commit]
  {:author (str (:author latest-commit) " <" (:email latest-commit) ">")
   :time  (to-timestamp (:time latest-commit))
   :hash (:id latest-commit)
   :message (:message latest-commit)})


(defn- ->id [id]
  {:id id})

(defn- throw! [reason]
  (throw (Exception. reason)))

(defn stream-logs [log-stream build-id phase f]
  (try
    (loop [line (<!! log-stream)]
      (when (not (nil? line))
        (let [event {:build build-id
                     :phase phase
                     :message (f line)}
              evt (insert-event event)]
          (events/send! :build-log-event (merge event evt {:ts (now)})))
        (recur (<!! log-stream))))
    (catch Exception e
      (info e)
      (info (.getNextException e)))))

(defn- push-logprocess [line]
  (-> line from-json process-message))

(defn status-update [build-id status msg]
  (let [s {:status status :id build-id :status-msg msg}]
    (update-build-status s)
    (events/send! :build-status-update s)))

(defn build [build-id build]
  (info "Check that docker answers" (ping))
  (info "Starting to build with ID:" build-id)
  (info "Git checkout from" (-> build :source_url))
  (status-update build-id "CHECKOUT" (-> build :source_url))
  (let [source-path (-> build :path)
        source-address (-> build :source_url)
        source-name (-> build :name)

        registry (-> build :registry_url)

        latest-commit (update-repo
                        source-address
                        source-path
                        (build->credentials build))
        tag (:id latest-commit)
        registry-name (str registry "/" source-name)
        tar (directory->tar source-path)]
    ; BUILD STEP
    (info "Repository checkout complete, commit:" latest-commit)
    (update-build-commit-data
      (merge
        (latest-commit->commit-data latest-commit)
        (->id build-id)))
    (events/send! :build-refresh {:id build-id})
    (status-update build-id "BUILDING" (:message latest-commit))
    (info "Making context archive and sending it into docker daemon ID:" build-id)
    (go
      (-> tar
          :files
          (stream-logs build-id "SEND-CONTEXT" strip-control-chars)))
    (info "Start docker build:" build-id)
    (-> tar
        :out
        (build-image source-name tag)
        (stream-logs build-id "BUILD" push-logprocess))
    ; TAG
    (info "Tag docker image" registry-name)
    (when-not (tag-image source-name registry-name tag)
      (error "Tagging failed")
      (status-update build-id "ERROR" "Tagging failed!")
      (throw! "Tagging failed"))

    (info "Push docker image" registry-name)
    (update-build-status {:status "PUSHING" :id build-id :status-msg registry-name})
    (status-update build-id "PUSHING" registry-name)
    ; PUSH TO REGISTRY
    (-> (push-image registry-name tag)
        (stream-logs build-id "PUSH" push-logprocess))
    (info "Build completed" build-id)
    (complete-build {:id build-id})
    (events/send! :build-refresh {:id build-id})))
