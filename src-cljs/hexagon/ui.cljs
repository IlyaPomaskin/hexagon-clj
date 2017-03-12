(ns hexagon.ui
  (:require [clojure.string :as string]
            [rum.core :as rum]
            [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.ws :as ws]
            [hexagon.utils :as utils]))

(defn cn [& args]
  (->> args
       (filterv #(not (or (nil? %1) (false? %1) (string/blank? %1))))
       (string/join " ")))

;; user list screen

(defn user [user-eid selected?]
  (let [b (d/entity @db user-eid)]
    [:div { :class (cn "c-card__item"
                       (when selected?
                         "c-card__item--active")) } (:user/name b)]))

(rum/defc users-list [selected-user-eid on-user-select]
  (let [all-users (d/q '[:find [?e ...]
                         :where [?e :user/name ?name]] @db)
        current-eid (utils/get-current-user-eid)
        users (filter #(not= current-eid %1) all-users)]
    (if (empty? users)
      [:.u-centered  "No users :("]
      [:ul.c-card.c-card--menu
       (map (fn [eid] [:li { :key eid
                             :on-click #(on-user-select (when-not (= selected-user-eid eid) eid)) }
                       (user eid (= selected-user-eid eid))])
            users)])))

(rum/defc boards-select < { :init (fn [{ [eid on-change] :rum/args }]
                                    (on-change (d/q '[:find ?v .
                                                      :where [?e :board/name ?v]] @db))) }
  [board-eid on-change]
  (let [boards (d/q '[:find ?e ?v
                      :where
                      [?e :board/name ?v]] @db)]
    [:.o-form-element
     [:label.c-label { :for "boards-list" } "Game board"]
     [:select.c-field
      { :id "boards-list"
        :on-change #(on-change (.. %1 -target -value)) }
      (map (fn [[eid name]] [:option { :key eid
                                       :value name } name])
           boards)]]))

(rum/defc first-move-toggle [toggled? on-change]
  [:.o-form-element
   [:label.c-toggle { :on-click #(on-change (not toggled?)) }
    [:input { :type "checkbox"
              :checked (not toggled?) }]
    [:.c-toggle__track [:.c-toggle__handle]]
    "Opponent first move"]])

(rum/defcs game-settings < (rum/local { :board-eid nil
                                        :owner-first-move? true } ::settings)
  [{ settings ::settings } user-eid]
  [:form { :on-submit #(do
                         (.preventDefault %1)
                         (ws/send! { :type "send-invite"
                                     :to (d/q '[:find ?v .
                                                :in $ ?eid
                                                :where [?eid :user/name ?v]] @db user-eid)
                                     :game-settings { :board (:board-eid @settings)
                                                      :owner-first-move? (:owner-first-move? @settings) } })) }
   [:fieldset.o-fieldset
    [:h3.c-heading.u-centered (str "Invite user #" user-eid)]
    (boards-select (:board-eid @settings) #(swap! settings assoc :board-eid %1))
    (first-move-toggle (:owner-first-move? @settings) #(swap! settings assoc :owner-first-move? %1))
    [:.o-form-element
     [:input.c-button { :type "submit"
                        :value "Invite" }]]]])

(rum/defcs users-screen < (rum/local nil ::selected-user-eid)
  [{ selected-user-eid ::selected-user-eid }]
  [:.o-grid
   [:.o-grid__cell.o-grid__cell--width-33
    (users-list @selected-user-eid #(reset! selected-user-eid %1))]
   [:.o-grid__cell.o-grid__cell--width-66
    (if (some? @selected-user-eid)
      (game-settings @selected-user-eid)
      [:.u-centered  "Select an user"])]])

;; game screen

;; root

(rum/defcs root < rum/reactive (rum/local users-screen ::screen)
  [{ screen ::screen } db]
  (let [db-sub (rum/react db)]
    [:.o-container.o-container--medium
     ;; [:div { :on-click #(reset! screen boards-list) } "Boards list"]
     [:div { :on-click #(reset! screen users-screen) } "Users list"]
     (@screen)]))
