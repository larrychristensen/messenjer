(ns messenjer.system
  (:require [com.stuartsierra.component :as component]
            [reloaded.repl :refer [init start stop go reset]]
            [io.pedestal.http :as http]
            [io.pedestal.http.jetty.websockets :as ws]
            [messenjer.pedestal :as pedestal]
            [messenjer.routes :as routes]
            [messenjer.kafka :as kafka]))

(def dev-service-map-overrides
  {::http/port 8890
   ::http/join? false
   ::http/routes #(deref #'routes/routes)
   ::http/allowed-origins {:creds true :allowed-origins (constantly true)}})

(defn prod-service-map [web-socket-clients producer]
  {::http/routes routes/routes
   ::http/type :jetty
   ;; this allows loading your port dynamically from, say, Heroku
   ;; or Elastic Beanstalk
   ::http/port (let [port-str (System/getenv "PORT")]
                 (if port-str (Integer/parseInt port-str)))
   ::http/join false
   ::http/resource-path "/public"
   ;; the default content-security-policy header is too restrictive to allow
   ;; dynamically loaded JS
   ::http/secure-headers {:content-security-policy-settings {:object-src "none"}}
   ;; add in our Web socket configuration
   ::http/container-options {:context-configurator #(ws/add-ws-endpoints % (routes/ws-paths web-socket-clients producer))}})

(defrecord ServiceMap [env web-socket-clients producer config]
  component/Lifecycle
  (start [this]
    (assoc this
           :config
           (cond-> (merge
                    {:env env}
                    (prod-service-map web-socket-clients producer)
                    (if (= :dev env) dev-service-map-overrides))
             true http/default-interceptors
             (= :dev env) http/dev-interceptors)))
  (stop [this]
    (assoc this :config nil)))

;; this is where we pull all our components together
(defn system [env]
  (component/system-map

   :web-socket-clients (atom {})

   :consumer
   (component/using
    (kafka/new-consumer)
    [:web-socket-clients])

   :producer
   (kafka/new-producer)

   ;; configure our Pedestal service map for our environment
   :service-map
   (component/using
    (map->ServiceMap {:env env})
    [:web-socket-clients
     :producer])

   ;; configure our Pedestal component, declaring :service-map
   ;; as a dependency. You can build pretty complicated systems
   ;; declaratively in this manner--by just declaring
   ;; dependencies--and Pedestal will build your system
   ;; dependency graph. This is the Clojure analog to dependency
   ;; injection frameworks such as Spring.
   :pedestal
   (component/using
    (pedestal/new-pedestal)
    [:service-map])))

(reloaded.repl/set-init! #(system :prod))
