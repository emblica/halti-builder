(ns halti-builder.api.registries.crud
  (:require [halti-builder.db.registries :as db]
            [halti-builder.utils :refer [json-request ->int]]
            [halti-builder.pg :refer [with-pg-connection]]))


(defn single-registry [registry-id]
  (let [get (with-pg-connection db/registry-by-id)]
    (json-request 200 (get {:id (->int registry-id)}))))

(defn registries-list [req]
  (let [all-registries (with-pg-connection db/all-registries)]
    (json-request 200 (all-registries))))

(defn create-registry [req]
  (json-request 200 req))

(defn delete-registry [registry-id]
  (json-request 200 {:id (->int registry-id)}))

(defn update-registry [req]
  (json-request 200 req))
