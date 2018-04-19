(ns messenjer.kafka
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as async])
  (:import [org.apache.kafka.clients.producer KafkaProducer ProducerRecord]
           [org.apache.kafka.clients.consumer KafkaConsumer]
           [org.apache.kafka.common.serialization StringDeserializer StringSerializer]))

(defprotocol Sender
  (send-message [this topic key message]))

(defrecord Producer []
  component/Lifecycle
  (start [this]
    (let [cfg {"value.serializer" StringSerializer
               "key.serializer" StringSerializer
               "bootstrap.servers" "localhost:9092"}]
      (assoc this :producer (KafkaProducer. cfg))))
  (stop [this]
    (assoc this :producer nil))

  Sender
  (send-message [this topic key message]
    (.send (:producer this)
           (ProducerRecord. topic (str key) (str message)))))

(defn new-producer []
  (map->Producer {}))

(defrecord Consumer [web-socket-clients consumer stopped?]
  component/Lifecycle
  (start [this]
    (let [cfg {"bootstrap.servers" "localhost:9092"
               "group.id" (str (java.util.UUID/randomUUID))
               "auto.offset.reset" "latest"
               "enable.auto.commit" "true"
               "key.deserializer" StringDeserializer
               "value.deserializer" StringDeserializer}
          consumer (doto (KafkaConsumer. cfg)
                     (.subscribe ["message"]))
          stopped? (atom false)]
      (async/go (while (not @stopped?)
            (let [messages (.poll consumer 100)]
              (doseq [message-record messages]
                (let [message (clojure.edn/read-string (.value message-record))
                      clients @web-socket-clients
                      client-info (clients (:to message))
                      send-chan (get client-info :channel)]
                  (if send-chan
                    (async/>! send-chan (str message))))))))
      (assoc this
             :consumer consumer
             :stopped? stopped?)))
  (stop [this]
    (try (.wakeup (:consumer this))
         (catch org.apache.kafka.common.errors.WakeupException e))
    (reset! (:stopped? this) true)
    (assoc this
           :consumer nil
           :stopped? nil)))

(defn new-consumer []
  (map->Consumer {}))
