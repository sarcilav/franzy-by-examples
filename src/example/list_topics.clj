(ns example.list-topics
  (:require [franzy.clients.consumer.client :as consumer]
            [franzy.common.metadata.protocols :as protocols]
            [franzy.serialization.nippy.deserializers :as nippy-deserializers]
            [franzy.serialization.deserializers :as deserializers]))

(def kafka-brokers [])

(defn consumer-topics-list []
  (let [cc {:bootstrap.servers kafka-brokers
            :group.id          "love-in-the-time-of-phone-apps"
            :client.id         "spoons-too-big"}
        key-serializer (deserializers/keyword-deserializer)
        value-serializer (nippy-deserializers/nippy-deserializer)]
    (with-open [p (consumer/make-consumer cc key-serializer value-serializer)]
      ;;Suppose we created a consumer and we want to see what topics are available to consumer from....
      ;;We could use admin, or just do this:
      (let [topics (protocols/list-topics p)
            custom-topic (:elk-custom topics)]
        custom-topic)
      ;;pick a topic and enjoy its riches or see which topics are having issues, blown up, etc.
      )))

;; (defn subscribing-consumer []
;;   (let [cc ]))
