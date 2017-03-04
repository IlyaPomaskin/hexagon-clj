(ns hexagon.main
  (:require [rum.core :as rum]
            [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.ws :as ws]
            [hexagon.ui :as ui]))

(enable-console-print!)

(def username (-> js/window .-__INITIAL_STATE__ .-username))

(defn handle-message [msg]
  (when (= (.-type msg) "datoms")
    (->> msg
         .-payload
         cljs.reader/read-string
         (d/transact! db))))

(defn init! []
  (when-some [ws @ws/chan] (.close ws))
  (ws/make! username handle-message)
  (rum/mount (ui/root db) (js/document.querySelector "#container")))

(init!)
