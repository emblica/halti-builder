(ns halti-builder.api.builds.routes
  (:require [compojure.core :refer :all]
            [halti-builder.api.builds.crud :as crud]
            [compojure.route :as route]))

(def router
  (routes
    (GET "/" [] crud/builds-list)
    (GET "/:build-id" [build-id] (crud/single-build build-id))
    (GET "/:build-id/logs" [build-id] (crud/build-logs build-id))
    (DELETE "/:build-id" [build-id] (crud/delete-build build-id))))
