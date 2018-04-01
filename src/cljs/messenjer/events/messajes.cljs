(ns messenjer.events.messajes
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [re-frame.core :refer [reg-event-db reg-event-fx reg-fx dispatch]]
            [cljs-http.client :as http]
            [cljs.core.async :as async]
            [cljs.reader :refer [read-string]]))

(defn handle-message
  "our Web socket message handler"
  [e]
  (let [parsed (read-string (.-data e))
        {:keys [type] :as event} parsed]
    (case type
      ;; event sent when the connection is initialized
      :connect (dispatch [:set-connection event])
      ;; event containing the list of other users, sent
      ;; shortly after connection is established
      :users (dispatch [:set-users (:users event)])
      ;; event sent when another user has been added
      ;; to the system
      :user (dispatch [:add-user (:user event)])
      ;; event sent when another user sends a message
      ;; to this user
      :message (dispatch [:add-messaje event])
      ;; ignore everything else
      nil)))

(defn new-web-socket []
  (let [host js/window.location.host
        ws (js/WebSocket. (str "ws://" host "/ws"))]
    (aset ws "onmessage" handle-message)
    ws))

;; this event primes the DB when the app is loaded
(reg-event-fx
 :initialize-db
 (fn [{:keys [db]}]
   {:db (if (seq db)
          db
          (let [ws (new-web-socket)]
            {:ws ws}))}))

(reg-event-db
 :select-user
 (fn [db [_ user-id]]
   (assoc db :other-user-id user-id)))

(reg-event-db
 :set-connection
 (fn [db [_ {:keys [id name]}]]
   (assoc db
          :user-id id
          :username name)))

;; register an effect handler to allow us to easily
;; send Web socket messages using the :ws-send FX
;; config
(reg-fx
 :ws-send
 (fn [event]
   (let [app-db re-frame.db/app-db
         db @app-db
         ws (get db :ws)]
     (.send ws event))))

(reg-event-db
 :set-users
 (fn [db [_ users]]
   (assoc db :users users)))

(reg-event-db
 :add-user
 (fn [db [_ user]]
   (update db :users conj user)))

(defn update-messages [db from messaje]
  (update-in db [:messajes from] #(-> (or % []) (conj messaje))))

(reg-event-db
 :add-messaje
 (fn [db [_ {:keys [from] :as messaje}]]
   (update-messages db from messaje)))

(reg-event-fx
 :send-messaje
 (fn [{:keys [db]} [_ user-id messaje]]
   (let [messaje-obj {:type :message
                      :from (get db :user-id)
                      :to user-id
                      :messaje messaje
                      :time (js/Date.)}]
     {:db (update-messages db user-id messaje-obj)
      :ws-send messaje-obj})))
