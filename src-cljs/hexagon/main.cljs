(ns hexagon.main
  (:require [rum.core :as rum]
            [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.ws :as ws]
            [hexagon.ui :as ui]))

(enable-console-print!)

(defn handle-message [msg]
  (when (= (.-type msg) "datoms")
    (->> msg
         .-payload
         cljs.reader/read-string
         (d/transact! db))))

(defn init! [initial-state]
  (let [username (.-username initial-state)]
    (when-some [ws @ws/chan]
               (do
                 (.close ws)
                 (db/reset-db!)))
    (ws/make! username handle-message)
    (rum/mount (ui/root db) (js/document.querySelector "#container"))))

(init! js/window.__INITIAL_STATE__)
