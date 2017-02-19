(ns hexagon.entities.invite
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.entities.board :as board]
            [hexagon.entities.timeout :as timeout]
            [hexagon.entities.game-settings :as game-settings]
            [hexagon.entities.user :as user]
            [hexagon.entities.invite :as invite]))

(defn add [from to settings]
  (d/transact! db [(assoc (game-settings/create-game-settings settings) :db/id -1)
                   { :invite/to (user/get-eid to)
                     :invite/from (user/get-eid from)
                     :invite/settings -1 }]))

(defn get [from to]
  (d/q '[:find ?e .
         :in $ ?from ?to
         :where
         [?e :invite/from ?from]
         [?e :invite/to ?to]] @db (user/get-eid from) (user/get-eid to)))

(defn exists? [from to]
  (some? (get from to)))

(defn get-by-user-eid [from]
  (db/entity-by-av :invite/from from))

(defn get-by-username [from]
  (get-by-user-eid (user/get-eid from)))
