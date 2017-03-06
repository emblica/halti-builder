(ns halti-builder.events
  (:require [clojure.core.async :as async :refer [go <! put! timeout chan tap sliding-buffer mult >! close!]]))


(defn batch [in out max-time max-count]
  (let [lim-1 (dec max-count)]
    (async/go-loop [buf [] t (async/timeout max-time)]
      (let [[v p] (async/alts! [in t])]
        (cond
          (= p t)
          (do
            (when-not (empty? buf)
              (async/>! out buf))
            (recur [] (async/timeout max-time)))

          (nil? v)
          (if (seq buf)
            (async/>! out buf))

          (== (count buf) lim-1)
          (do
            (async/>! out (conj buf v))
            (recur [] (async/timeout max-time)))

          :else
          (recur (conj buf v) t))))))



(def publish-channel (chan))
(def subscribe-channel (mult publish-channel))

(def subscribers (atom []))

(defn send! [type payload]
  (put! publish-channel {:type type :payload payload}))
