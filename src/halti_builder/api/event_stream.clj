(ns halti-builder.api.event-stream
  (:require [clojure.core.async :as async :refer :all :exclude [map into reduce merge partition partition-by take]]
            [halti-builder.utils :refer [json-request ->int to-json]]
            [halti-builder.events :as events]
            [chord.http-kit :refer [with-channel]]
            [clj-time.coerce :as coer]
            [cheshire.generate :refer [add-encoder encode-str remove-encoder]]))


(add-encoder  org.joda.time.DateTime
              (fn [c jg]
                (.writeString jg (coer/to-string c))))

(defn stream [req]
  (with-channel req ws-chan {:format :json-kw}
    ;(>!! ws-chan "{\"status\":\"OK\"}")
    (let [ec (chan)
          batched-events (chan)]
      (events/batch ec batched-events 200 1000)
      (tap events/subscribe-channel ec)
      (go
        (loop []
          (>! ws-chan (<! batched-events))
          (recur))))))
