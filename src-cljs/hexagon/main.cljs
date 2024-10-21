(ns hexagon.main
  (:require [rum.core :as rum]
            [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.ws :as ws]
            [hexagon.ui :as ui]
            [hexagon.utils :as utils]))

(enable-console-print!)

(defn handle-message [msg]
  (when (= (.-type msg) "datoms")
    (let [datoms (cljs.reader/read-string (.-payload msg))]
      (cljs.pprint/pprint datoms)
      (d/transact! db datoms))))

(defn init! []
  (ws/close!)
  (ws/make! utils/current-username handle-message)
  (rum/mount (ui/root db) (js/document.querySelector "#container")))

(init!)
