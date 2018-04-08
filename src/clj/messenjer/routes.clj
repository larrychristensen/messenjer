(ns messenjer.routes
  (:require [io.pedestal.http.jetty.websockets :as ws]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [clojure.java.io :as io]
            [clojure.core.async :as async]
            [messenjer.kafka :as kafka]))

(defn send-message-to-all!
  [message ws-clients]
  (doseq [[id {:keys [session channel]}] @ws-clients]
    (async/put! channel message)))

(defn new-ws-client!
  [ws-clients ws-session send-ch]
  ;; set our timeout to 1 hour, really we should do more sophisticated
  ;; connection handling
  (.setIdleTimeout ws-session (* 1000 60 60))

  ;; assign a user ID and username to the new connection, in a real
  ;; messaging app we would let the user create an account and use
  ;; persistent user IDs and usernames
  (let [id (java.util.UUID/randomUUID)
        name (str "User " (count @ws-clients))]
    ;; let the client know its user info
    (async/put! send-ch (str {:type :connect
                              :id id
                              :name name}))
    ;; send the client the current user list
    (async/put! send-ch (str {:type :users
                              :users (map
                                      (fn [[id {:keys [username]}]]
                                        {:id id
                                         :name username})
                                      @ws-clients)}))
    ;; notify all other clients that a new user has joined
    (send-message-to-all! (str {:type :user
                                :user {:id id
                                       :name name}})
                          ws-clients)
    ;; store our session info for the new user
    (swap! ws-clients
           assoc
           id
           {:username name
            :session ws-session
            :channel send-ch})))

;; our Web socket configuration, the connection URL will look like
;; ws://<host>/ws. We should be handling :on-close and :on-error
;; for more sophisticated connection handling.
(defn ws-paths [web-service-clients producer]
  {"/ws" {:on-connect (ws/start-ws-connection (partial new-ws-client! web-service-clients))
          :on-text (fn [msg]
                     (let [event (clojure.edn/read-string msg)]
                       (kafka/send-message producer
                                           "message"
                                           (:to event)
                                           event)))}})

;; get the index page. Really we should move the code in
;; src/cljs/messenjer/views/messaje.cljs into src/cljc and
;; dynamically render the index page here as hiccup
(defn index [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (io/file "resources/public/index.html")})

;; since pedestal handles routing of all other static resources
;; and we are using Web sockets for all of our other data, this
;; is the only route we need.
(defroutes routes
  [[["/" {:get index}]]])
