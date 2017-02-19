(ns hexagon.entities.game-settings
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.hex :as hex]
            [hexagon.entities.boards :as boards]
            [hexagon.entities.timeouts :as timeouts]))

;; game-settings

(defn create-game-settings [settings]
  (let [board-eid (-> settings :board boards/get-board :db/id (or boards/default-board))
        timeout-eid (-> settings :timeout timeouts/get-timeout :db/id (or timeouts/default-timeout))
        owner-first-move? (boolean (:owner-first-move settings))]
    { :game-settings/board board-eid
      :game-settings/timeout board-eid
      :game-settings/owner-first-move? owner-first-move? }))
