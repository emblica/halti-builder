(ns halti-builder.api.builds.crud
  (:require [halti-builder.db.builds :as db]
            [clojure.core.async :as async :refer :all :exclude [map into reduce merge partition partition-by take]]
            [halti-builder.utils :refer [json-request ->int to-json]]
            [halti-builder.pg :refer [with-pg-connection]]
            [halti-builder.events :as events]
            [chord.http-kit :refer [with-channel]]
            [schema.core :as s]))



(def Build
  {(s/optional-key :author) s/Str
   (s/optional-key :tag) s/Str
   :branch s/Str
   :image s/Int})

(defn insert-build [build]
  (let [ok? (s/validate Build build)
        insert! (with-pg-connection db/insert-build)]
    (when ok?
      (insert! build))))


(defn single-build [build-id]
  (let [get (with-pg-connection db/build-by-id)
        build (get {:id (->int build-id)})]
    (if (nil? build)
      (json-request 404 "Not found")
      (json-request 200 build))))


(defn builds-list [req]
  (let [all-builds (with-pg-connection db/all-builds)]
    (json-request 200 (all-builds))))

(defn build-logs [build-id]
  (let [all-logs (with-pg-connection db/logs-by-build-id)]
    (json-request 200 {:build (->int build-id)
                       :logs (map #(dissoc % :build) (all-logs {:id (->int build-id)}))})))


(defn delete-build [build-id]
  (json-request 200 {:id (->int build-id)}))

(defn update-build [req]
  (json-request 200 req))
