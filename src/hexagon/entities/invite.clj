(ns hexagon.entities.invite
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.hex :as hex]
            [hexagon.entities.board :as board]
            [hexagon.entities.timeout :as timeout]
            [hexagon.entities.game-settings :as game-settings]
            [hexagon.entities.user :as user]))

;; invites

(defn add-invite [from to settings]
  (d/transact! db [(assoc (game-settings/create-game-settings settings) :db/id -1)
                   { :invite/to (users/get-user-eid to)
                     :invite/from (users/get-user-eid from)
                     :invite/settings -1 }]))

(defn get-invite [from to]
  (d/q '[:find ?e .
         :in $ ?from ?to
         :where
         [?e :invite/from ?from]
         [?e :invite/to ?to]] @db (users/get-user-eid from) (users/get-user-eid to)))

(defn invite-exists? [from to]
  (some? (get-invite from to)))

(defn get-invite-by-user-eid [from]
  (db/entity-by-av :invite/from from))

(defn get-invite-by-username [from]
  (get-invite-by-user-eid (users/get-user-eid from)))
