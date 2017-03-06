(ns halti-builder.api.sources.routes
  (:require [compojure.core :refer :all]
            [halti-builder.api.sources.crud :as crud]
            [compojure.route :as route]))

(def router
  (routes
    (GET "/" [] crud/sources-list)
    (POST "/" [] crud/create-source)
    (PUT "/:source-id" [] crud/update-source)
    (DELETE "/:source-id" [] crud/delete-source)))
