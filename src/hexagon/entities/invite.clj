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

(defn get-eid [from to]
  (let [from-eid (user/get-eid from)
        to-eid (user/get-eid to)]
    (when (and (some? from-eid)
               (some? to-eid))
      (d/q '[:find ?e .
             :in $ ?from ?to
             :where
             [?e :invite/from ?from]
             [?e :invite/to ?to]] @db from-eid to-eid))))

(defn get [from to]
  (when-let [eid (get-eid from to)]
    (d/entity @db eid)))

(defn exists? [from to]
  (some? (get-eid from to)))

(defn get-by-user-eid [from]
  (db/entity-by-av :invite/from from))

(defn get-by-username [from]
  (get-by-user-eid (user/get-eid from)))

(defn serialize [invite]
  { :to (:user/name (:invite/to invite))
    :from (:user/name (:invite/from invite))
    :settings (game-settings/serialize (:invite/settings invite)) })
