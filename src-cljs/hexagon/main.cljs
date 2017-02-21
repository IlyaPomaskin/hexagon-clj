(ns hexagon.main
  (:require [om.core :as om :include-macros true]
            [om.dom :as D :include-macros true]))

(enable-console-print!)

(defn widget [props owner]
  (reify
    om/IInitState
    (init-state [_]
                {:value "" :count 0})
    om/IRenderState
    (render-state [_ {:keys [value]}]
                  (D/div nil
                         (D/label nil "Only numeric : ")
                         (D/input #js
                                  {:value value
                                   :onChange
                                   #(let [new-value (-> % .-target .-value)]
                                      (if (js/isNaN new-value)
                                        (om/set-state! owner :value value)
                                        (om/set-state! owner :value new-value)))})))))

(om/root widget
         {}
         {:target (. js/document (querySelector "#container"))})
