(ns halti-builder.utils
  (:require [clojure.data.json :as json]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clojure.core.async :as async :refer :all :exclude [map into reduce merge partition partition-by take]]
            [clojure.java.io :as io]))

(defn stream->channel [stream]
  (-> stream
    io/reader
    line-seq
    to-chan))

(defn ->int [x]
  (Integer/parseInt
    (apply str (filter #(Character/isDigit %) x))))

(defn env [k v]
  (or (System/getenv k) v))

(defn flip [function]
  (fn
    ([] (function))
    ([x] (function x))
    ([x y] (function y x))
    ([x y z] (function z y x))
    ([a b c d] (function d c b a))
    ([a b c d & rest]
     (->> rest
        (concat [a b c d])
        reverse
        (apply function)))))

(defn from-json [s]
  (json/read-str s :key-fn keyword))

(defn to-json [o]
  (json/write-str o))

(defn json-request [status body]
  {:status  status
   :headers {"Content-Type" "application/json"}
   :body    (to-json body)})


(defn uuid [] (str (java.util.UUID/randomUUID)))

(extend-type java.sql.Timestamp
  json/JSONWriter
  (-write [date out]
    (json/-write (c/to-string (c/from-sql-time date)) out)))

(extend-type org.joda.time.DateTime
  json/JSONWriter
  (-write [date out]
    (json/-write (c/to-string date) out)))
