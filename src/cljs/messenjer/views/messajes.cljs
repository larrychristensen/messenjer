(ns messenjer.views.messajes
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :refer [atom]]))

(defn event-value [event]
  "Get the value out of a React DOM event"
  (.. event -target -value))

(defn user [name selected?]
  [:div.flex.align-items-center.p-5.p-l-15
   [:div.f-s-10.m-r-5.p-b-3 {:class-name (if selected? "white" "green")} "⬤"]
   [:div {:class-name (if selected? "white")} name]])

(defn user-list []
  [:div.w-220.bg-purple
   [:div.p-t-10.p-l-15.f-s-20.white.bold
    "Messenjer"]
   [:div.white [user @(subscribe [:username]) false]]
   (let [users @(subscribe [:users])
         other-user-id @(subscribe [:other-user-id])]
     [:div.gray.m-t-20
      [:div.p-l-15.m-b-5 "Users"]
      (doall
       (map
        (fn [{:keys [name id]}]
          (let [selected? (= id other-user-id)]
            ^{:key id}
            [:div.pointer
             {:class-name (if selected? "bg-green")
              :on-click #(dispatch [:select-user id])}
             [user name selected?]]))
        users))])])

(defn messaje-box []
  ;; this is how to do local state in Reagent--by building a closure
  ;; over the mutable state. Without the let and the lambda, your
  ;; component will not re-render. We want local state here because
  ;; we don't really want the rest of the application to care about
  ;; the mechanics of our messaje-box, it really only cares when
  ;; a messaje has been submitted. This approach is also a significant
  ;; optimization, since re-frame events don't need to be fired,
  ;; subscriptions don't need to be updated and no other components need
  ;; to figure out if they need to re-render. This is one of the
  ;; beautiful things about React, the biggest optimizations actually
  ;; tend to make your code better, not worse.
  (let [v (atom "")]
    (fn []
      (let [other-user-id @(subscribe [:other-user-id])]
        [:input.input
         {:value @v
          :on-change #(let [value (event-value %)]
                        (reset! v value))
          :on-key-press #(if (= (.-key %) "Enter")
                           (do (dispatch [:send-messaje other-user-id @v])
                               (reset! v "")))}]))))

(defn messajes []
  (let [other-user-id @(subscribe [:other-user-id])
        ms @(subscribe [:messajes other-user-id])]
    [:div.flex.flex-column-reverse.h-100-p
     [:div.p-l-10.overflow-auto
      (doall
       (map-indexed
        (fn [i {:keys [messaje to from time]}]
          ;; you get ugly warnings if you don't specify a key for
          ;; each sub-component here since React uses these IDs to
          ;; identify components and when they should be added to
          ;; or removed from the DOM.
          ^{:key i}
          [:div.p-10
           [:div.p-b-5
            [:span.bold @(subscribe [:username-for-id from])]
            [:span.dark-gray.f-s-12.m-l-5 (str time)]]
           [:div messaje]])
        ms))]]))

(defn messajes-page []
  [:div.flex.h-100-p
   [user-list]
   [:div.flex-grow-1.flex.flex-column
    [:div.flex-grow-1]
    [messajes]
    [:div.p-20 [messaje-box]]]])
