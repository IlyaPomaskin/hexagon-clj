(ns hexagon.main
  (:require [rum.core :as rum]
            [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.ws :as ws]))

(enable-console-print!)

(defn handle-message [msg]
  (when (= (.-type msg) "datoms")
    (->> msg
         .-payload
         cljs.reader/read-string
         (d/transact! db))))

(rum/defcs username-input < (rum/local "" ::username) [{ username ::username }]
  [ :form { :on-submit (fn [e]
                         (js/console.log @username)
                         (.preventDefault e)) }
    [:label { :html-for "username" } "Enter username"]
    [:br]
    [:input { :type "text"
              :id "username"
              :value @username
              :on-change (fn [e] (reset! username (.. e -target -value))) }]
    [:input { :type "submit" }]])

(defn init! []
  (when-some [ws @ws/chan]
             (do
               (.close ws)
               (db/reset-db!)))
  (ws/make! "username2" handle-message)
  (rum/mount (username-input) (. js/document (querySelector "#container"))))

(init!)
