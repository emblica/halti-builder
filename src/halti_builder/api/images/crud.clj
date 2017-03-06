(ns halti-builder.api.images.crud
  (:require [halti-builder.db.images :as db]
            [clojure.core.async :as async :refer :all :exclude [map into reduce merge partition partition-by take]]
            [halti-builder.utils :refer [json-request ->int]]
            [halti-builder.api.build :refer [build]]
            [halti-builder.api.builds.crud :refer [insert-build]]
            [halti-builder.pg :refer [with-pg-connection]]))


(defn- strip-private-key [source]
  (assoc source :private_key "************"))

(def get-image (with-pg-connection db/image-by-id))

(defn single-image [image-id]
  (json-request 200 (-> (get-image {:id (->int image-id)})
                        (strip-private-key))))

(defn images-list [req]
  (let [all-images (with-pg-connection db/all-images)]
    (json-request 200 (map strip-private-key (all-images)))))


(defn build-image [image-id]
  (let [image (get-image {:id (->int image-id)})
        build-id (:id (insert-build {:image (-> image :id)
                                     :author "SYSTEM"
                                     :tag ""
                                     :branch (-> image :branch)}))]
    (go (build build-id image))
    (json-request 200 {:build build-id})))

(defn create-image [req]
  (json-request 200 req))

(defn delete-image [image-id]
  (json-request 200 {:id (->int image-id)}))

(defn update-image [req]
  (json-request 200 req))
