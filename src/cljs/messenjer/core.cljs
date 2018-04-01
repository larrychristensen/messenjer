(ns messenjer.core
  (:require [reagent.core :as reagent]
            [messenjer.views.messajes :as mv]
            [messenjer.subs.messajes]
            [messenjer.events.messajes]
            [re-frame.core :refer [dispatch-sync]]))

(enable-console-print!)

(dispatch-sync [:initialize-db])

(reagent/render-component [mv/messajes-page]
                          (. js/document (getElementById "app")))
