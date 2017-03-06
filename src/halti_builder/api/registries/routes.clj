(ns halti-builder.api.registries.routes
  (:require [compojure.core :refer :all]
            [halti-builder.api.registries.crud :as crud]
            [compojure.route :as route]))

(def router
  (routes
    (GET "/" [] crud/registries-list)
    (POST "/" [] crud/create-registry)
    (PUT "/:registry-id" [] crud/update-registry)
    (DELETE "/:registry-id" [] crud/delete-registry)))
