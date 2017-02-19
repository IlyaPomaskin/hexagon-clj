(ns hexagon.entities.game-settings
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.entities.board :as board]
            [hexagon.entities.timeout :as timeout]))

(defn create-game-settings [settings]
  (let [board-eid (-> settings :board board/get :db/id (or board/default))
        timeout-eid (-> settings :timeout timeout/get :db/id (or timeout/default))
        owner-first-move? (boolean (:owner-first-move settings))]
    { :game-settings/board board-eid
      :game-settings/timeout board-eid
      :game-settings/owner-first-move? owner-first-move? }))

(defn get-by-game [game]
  (db/entity-by-eid (:game/settings game)))
