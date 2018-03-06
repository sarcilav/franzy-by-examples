(ns example.list-topics
  (:require [franzy.clients.consumer.client :as consumer]
            [franzy.common.metadata.protocols :as protocols]
            [franzy.clients.consumer.protocols :as pt]
            [franzy.serialization.nippy.deserializers :as nippy-deserializers]
            [franzy.common.models.types :as mt]
            [franzy.clients.consumer.defaults :as cd]
            [franzy.clients.consumer.callbacks :as callbacks]
            [taoensso.timbre :as timbre]
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

(defn subscribing-consumer []
  (let [cc {:bootstrap.servers       kafka-brokers
            :group.id                "submissive-blonde-aussies"
            :auto.offset.reset       :earliest
            ;;here we turn on committing offsets to Kafka itself, every 1000 ms
            :enable.auto.commit      true
            :auto.commit.interval.ms 1000}
        key-deserializer (deserializers/keyword-deserializer)
        value-deserializer (nippy-deserializers/nippy-deserializer)
        topic (:nippy-topic config/topics-to-create)
        ;;demonstrating using the topic partition record for kicks....
        ;;if you have a lot of partitions, it's better to allocate these than maps
        topic-partitions [(mt/->TopicPartition topic 0) (mt/->TopicPartition topic 1) (mt/->TopicPartition topic 2)]
        ;;Here we are demonstrating the use of a consumer rebalance listener. Normally you'd use this with a manual consumer to deal with offset management.
        ;;As more consumers join the consumer group, this callback should get fired among other reasons.
        ;;To implement a manual consumer without this function is folly, unless you care about losing data, and probably your job.
        ;;One could argue though that most data is not as valuable as we are told. I heard this in a dream once or in intro to Philosophy.
        rebalance-listener (callbacks/consumer-rebalance-listener (fn [topic-partitions]
                                                                    (timbre/info "topic partitions assigned:" topic-partitions))
                                                                  (fn [topic-partitions]
                                                                    (timbre/info "topic partitions revoked:" topic-partitions)))
        ;;We create custom producer options and set out listener callback like so.
        ;;Now we can avoid passing this callback every call that requires it, if we so desire
        ;;Avoiding the extra cost of creating and garbage collecting a listener is a best practice
        options (cd/make-default-consumer-options {:rebalance-listener-callback rebalance-listener})]
    (with-open [c (consumer/make-consumer cc key-deserializer value-deserializer options)]
      ;;Note! - The subscription will read your comitted offsets to position the consumer accordingly
      ;;If you see no data, try changing the consumer group temporarily
      ;;If still no, have a look inside Kafka itself, perhaps with franzy-admin!
      ;;Alternatively, you can setup another threat that will produce to your topic while you consume, and all should be well
      (pt/subscribe-to-partitions! c [topic])
      ;;Let's see what we subscribed to, we don't need Cumberbatch to investigate here...
      (println "Partitions subscribed to:" (pt/partition-subscriptions c))

      (let [cr (pt/poll! c)
            filter-xf (filter (fn [cr] (= (:key cr) :vizzini)))
            value-xf (map (fn [cr] (:value cr)))
            inconceivable-transduction (comp filter-xf value-xf)]

        (println "Record count:" (pt/record-count cr))
        (println "Records by topic:" (pt/records-by-topic cr topic))
        (println "Records from a topic that doesn't exist:" (pt/records-by-topic cr "no-one-of-consequence-999"))
        (println "Records by topic partition:" (pt/records-by-topic-partition cr (first topic-partitions)))
        ;;;The source data is a list, so no worries here....
        (println "Records by a topic partition that doesn't exist:" (pt/records-by-topic-partition cr "no-one-of-consequence" 99))
        (println "Topic Partitions in the result set:" (pt/record-partitions cr))
        (clojure.pprint/pprint (into [] inconceivable-transduction cr))
        (println "Put all the records into a vector (calls IReduceInit):" (into [] cr))
        ;;wow, that was tiring, maybe now we don't want to listen anymore to this topic and take a break, maybe subscribe
        ;;to something else next poll....
        (pt/clear-subscriptions! c)
        (println "After clearing subscriptions, a stunning development! We are now subscribed to the following partitions:"
                 (pt/partition-subscriptions c))))))
