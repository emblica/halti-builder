(ns halti-builder.api.routes
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [halti-builder.api.sources.routes :as sources]
            [halti-builder.api.registries.routes :as registries]
            [halti-builder.api.builds.routes :as builds]
            [halti-builder.api.images.routes :as images]
            [halti-builder.api.event-stream :as event-stream]
            [halti-builder.utils :refer [json-request]]
            [ring.middleware.json :refer [wrap-json-body]]))


(defn api-listing [req]
  (json-request 200 {:endpoints ["images" "builds" "sources" "registries"]}))



(def api-router
  (wrap-json-body
    (routes
      (GET "/" [] api-listing)
      (GET "/stream" [req] event-stream/stream)
      (context "/builds" [] builds/router)
      (context "/images" [] images/router)
      (context "/registries" [] registries/router)
      (context "/sources" [] sources/router))
    {:keywords? true}))
