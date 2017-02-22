(ns hexagon.ui
  (:require [rum.core :as rum]
            [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.ws :as ws]))

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

(defn board [board-eid]
  (let [b (d/entity @db board-eid)]
    [:span (:board/name b)]))

(defn boards-list []
  (let [boards (d/q '[:find [?e ...]
                      :where
                      [?e :board/name ?v]] @db)]
    [:ul
     (mapv #(vector [:li { :key %1 } (board %1)]) boards)]))

(rum/defc game-field [] [:div])

(rum/defc root < rum/reactive [db]
  (let [db-sub (rum/react db)]
    [:div
     [:dev.left (boards-list)]
     [:div.right (game-field)]]))
