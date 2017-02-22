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

(defn init! [usr]
  (when-some [ws @ws/chan]
             (do
               (.close ws)
               (db/reset-db!)))
  (ws/make! usr handle-message)
  (rum/mount (ui/root db) (js/document.querySelector "#container")))

(init! "username2")
