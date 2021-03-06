(ns messenjer.subs.messajes
  (:require [re-frame.core :refer [reg-sub reg-sub-raw subscribe dispatch]]))

(reg-sub
 :db
 (fn [db _]
   db))

(reg-sub
 :users
 (fn [db _]
   (get db :users [])))

(reg-sub
 :users-map
 :<- [:users]
 :<- [:user-id]
 :<- [:username]
 (fn [[users id username] _]
   (assoc (into {} (map (juxt :id identity)) users)
          id {:name username})))

(reg-sub
 :user-id
 (fn [db _]
   (get db :user-id)))

(reg-sub
 :username
 (fn [db _]
   (get db :username)))

(reg-sub
 :username-for-id
 :<- [:users-map]
 (fn [users-map [_ id]]
   (-> id users-map :name)))

(reg-sub
 :other-user-id
 :<- [:db]
 :<- [:users]
 (fn [[db users] _]
   (get db :other-user-id (-> users first :id))))

(reg-sub
 :messajes
 (fn [db [_ other-user-id]]
   (get-in db [:messajes other-user-id] [])))

