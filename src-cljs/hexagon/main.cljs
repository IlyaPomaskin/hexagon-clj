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

(rum/defcs stateful < (rum/local 0 ::key)
  [state label]
  (let [local-atom (::key state)]
    [:div { :on-click (fn [_] (swap! local-atom inc)) }
     label ": " @local-atom]))

(defn init! []
  (when-some [ws @ws/chan]
             (do
               (.close ws)
               (db/reset!)))
  (ws/make! "username2" handle-message)
  (rum/mount (stateful "Click count") (. js/document (querySelector "#container"))))

(init!)
