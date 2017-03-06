(ns halti-builder.docker
  (:require [clj-http.client :as client]
            [clojure.core.async :as async :refer :all :exclude [map into reduce merge partition partition-by take]]
            [halti-builder.utils :refer [env from-json stream->channel]]
            [taoensso.timbre :as timbre :refer [spy info error]]
            [clojure.java.io :as io]))

(def API-ROOT (env "DOCKER_URL" "http://localhost:2376/v1.26"))


(defn ping []
  (-> (client/get (str API-ROOT "/_ping"))
    :body
    (= "OK")))

(defn ->tag [name tag]
  (str name ":" tag))

(defn build-image
  ([buildpack name] (build-image buildpack name "latest"))
  ([buildpack name tag]
   (let [url (str API-ROOT "/build")
         options {:throw-exceptions false
                  :query-params {:t (->tag name tag)}
                  :content-type "archive/tar"
                  :as :stream
                  :body buildpack}
         request (client/post url options)]
     (-> request
       :body
       stream->channel))))


(defn tag-image
  ([image repo] (tag-image image repo "latest"))
  ([image repo tag]
   (let [url (str API-ROOT "/images/" (->tag image tag) "/tag")
         options {:throw-exceptions false
                  :query-params {:repo repo
                                 :tag tag}}]
      (-> (spy (client/post url options))
        :status
        (= 201)))))

(defn push-image
  ([image] (push-image image "latest"))
  ([image tag]
   (let [url (str API-ROOT "/images/" image "/push")
         options {:throw-exceptions false
                  :query-params {:tag tag}
                  :headers {"X-Registry-Auth" "xxx"}
                  :as :stream}]
      (-> (client/post url options)
          :body
          stream->channel))))
