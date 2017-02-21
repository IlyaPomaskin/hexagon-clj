(ns hexagon.main
  (:require [rum.core :as rum]))

(enable-console-print!)

(rum/defcs stateful < (rum/local 0 ::key)
  [state label]
  (let [local-atom (::key state)]
    [:div { :on-click (fn [_] (swap! local-atom inc)) }
     label ": " @local-atom]))

(defn init! []
  (rum/mount (stateful "Click count") (. js/document (querySelector "#container"))))

(init!)
