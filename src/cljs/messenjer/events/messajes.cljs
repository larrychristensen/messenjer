(ns messenjer.events.messajes
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [re-frame.core :refer [reg-event-db reg-event-fx reg-fx dispatch]]
            [cljs-http.client :as http]
            [cljs.core.async :as async]
            [cljs.reader :refer [read-string]]))

(reg-event-db
 :select-user
 (fn [db [_ user-id]]
   (assoc db :other-user-id user-id)))

(defn update-messages [db from messaje]
  (update-in db [:messajes from] #(-> (or % []) (conj messaje))))

(reg-event-db
 :send-messaje
 (fn [db [_ user-id messaje]]
   (let [messaje-obj {:type :message
                      :from 0
                      :to user-id
                      :messaje messaje
                      :time (js/Date.)}]
     (update-messages db user-id messaje-obj))))
