(ns halti-builder.pg
  (:require [hikari-cp.core :refer :all]
            [halti-builder.utils :refer [env to-json from-json]]
            [clojure.java.jdbc :as jdbc])
  (:import org.postgresql.util.PGobject))


(def datasource-options
  "PostgreSQL options"
  {:auto-commit        true
   :read-only          false
   :connection-timeout 3000
   :validation-timeout 5000
   :idle-timeout       600000
   :max-lifetime       1800000
   :minimum-idle       10
   :maximum-pool-size  200
   :pool-name          "halti-builder"})

(def datasource (atom nil))

(defn init-database! []
  (let [pg-uri {:jdbc-url (env "DATABASE_URL" "jdbc:postgresql://localhost:32768/postgres?user=postgres")}
        full-options (merge datasource-options pg-uri)]
    (reset! datasource {:datasource (make-datasource full-options)})))

(defn with-pg-connection
  "Wrap data processing function into postgresql transaction.
   If something fails during the processing, the transaction is rollbacked"
  [f]
  (fn [& args]
    (jdbc/with-db-connection [conn @datasource]
      (apply f (cons conn args)))))


(defn value-to-json-pgobject [value]
  (doto (PGobject.)
    (.setType "json")
    (.setValue (to-json value))))

(extend-protocol jdbc/ISQLValue
  clojure.lang.IPersistentMap
  (sql-value [value] (value-to-json-pgobject value))

  clojure.lang.IPersistentVector
  (sql-value [value] (value-to-json-pgobject value)))

(extend-protocol jdbc/IResultSetReadColumn
  PGobject
  (result-set-read-column [pgobj metadata idx]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "json" (from-json value)
        :else value))))
