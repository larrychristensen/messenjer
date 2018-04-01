(ns messenjer.routes
  (:require [io.pedestal.http.jetty.websockets :as ws]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [clojure.core.async :as async]))

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
