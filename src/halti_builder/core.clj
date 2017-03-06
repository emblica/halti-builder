(ns halti-builder.core
  (:require [halti-builder.routes :refer [main-router]]
            [org.httpkit.server :refer [run-server]]
            [compojure.handler :refer [site]]
            [taoensso.timbre :as timbre :refer [info error warn debug]]
            [ring.middleware.reload :as reload]
            [halti-builder.utils :refer [env]]
            [halti-builder.pg :refer [init-database!]]
            [ring.middleware.logger :refer [wrap-with-logger]])
  (:gen-class))




(def PORT (Integer/parseInt (env "PORT" "4041")))

(def in-dev? (= "no" (env "PRODUCTION" "no")))


(defn wrap-with-simple-logging [handler]
  (fn [request]
    (info (:request-method request) " - " (:uri request))
    (handler request)))


(defn -main
  "halti-builder main function"
  [& args]
  (info "Halti builder - starting up! server @ 0.0.0.0:" PORT)
  (let [handler (if in-dev?
                  (reload/wrap-reload (site #'main-router)) ;; only reload when dev
                  (site main-router))]
    (init-database!) ; Initialize PG connection pool
    (run-server (wrap-with-simple-logging handler) {:port PORT})))
