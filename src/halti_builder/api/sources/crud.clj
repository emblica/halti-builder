(ns halti-builder.api.sources.crud
  (:require [halti-builder.db.images :as db]
            [halti-builder.utils :refer [json-request ->int]]
            [halti-builder.pg :refer [with-pg-connection]]))


(defn- strip-private-key [source]
  (assoc source :private_key "************"))

(defn single-source [source-id]
  (let [get (with-pg-connection db/source-by-id)]
    (json-request 200 (-> (get (->int source-id))
                          (strip-private-key)))))

(defn sources-list [req]
  (let [all-sources (with-pg-connection db/all-sources)]
    (json-request 200 (map strip-private-key (all-sources)))))

(defn create-source [req]
  (json-request 200 req))

(defn delete-source [source-id]
  (json-request 200 {:id (->int source-id)}))

(defn update-source [req]
  (json-request 200 req))
