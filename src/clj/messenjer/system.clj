(ns messenjer.system
  (:require [com.stuartsierra.component :as component]       
            [io.pedestal.http :as http]
            [io.pedestal.http.jetty.websockets :as ws]
            [messenjer.pedestal :as pedestal]
            [messenjer.routes :as routes]))

(def dev-service-map-overrides
  {::http/port 8890
   ::http/join? false
   ::http/routes #(deref #'routes/routes)
   ::http/allowed-origins {:creds true :allowed-origins (constantly true)}})

(def prod-service-map
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
   ::http/secure-headers {:content-security-policy-settings {:object-src "none"}}})

;; this is where we pull all our components together
(defn system [env]
  (component/system-map

   ;; configure our Pedestal service map for our environment
   :service-map
   (cond-> (merge
            {:env env}
            prod-service-map
            (if (= :dev env) dev-service-map-overrides))
     true http/default-interceptors
     (= :dev env) http/dev-interceptors)

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
