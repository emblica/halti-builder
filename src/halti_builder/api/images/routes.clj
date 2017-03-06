(ns halti-builder.api.images.routes
  (:require [compojure.core :refer :all]
            [halti-builder.api.images.crud :as crud]
            [compojure.route :as route]))

(def router
  (routes
    (GET "/" [] crud/images-list)
    (POST "/" [] crud/create-image)
    (GET "/:image-id" [image-id] (crud/single-image image-id))
    (PUT "/:image-id" [] crud/update-image)
    (GET "/:image-id/build" [image-id] (crud/build-image image-id))
    (DELETE "/:image-id" [image-id] (crud/delete-image image-id))))
