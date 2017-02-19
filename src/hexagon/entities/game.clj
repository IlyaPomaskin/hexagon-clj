(ns hexagon.entities.game
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.entities.board :as board]
            [hexagon.entities.timeout :as timeout]
            [hexagon.entities.invite :as invite]
            [hexagon.entities.user :as user]
            [hexagon.entities.game-settings :as game-settings]))

(defn board-cell->game-cell [game cell]
  { :cell/x (:x cell)
    :cell/y (:y cell)
    :cell/type (:type cell)
    :cell/owner (case (:owner cell)
                  :red (:game/red game)
                  :blue (:game/blue game)
                  nil)
    :cell/game (:db/id game) })

(defn create-game-board-cells [game]
  (let [board-map (d/q '[:find ?map .
                         :in $ ?eid
                         :where
                         [?board :board/map ?map]
                         [?eid :game-settings/board ?board]] @db (:game/settings game))]
    (mapv (partial board-cell->game-cell game) board-map)))

(defn make-game [invite]
  (let [{ from :invite/from
          to :invite/to
          settings :invite/settings } invite
        blue (if (:game-settings/owner-first-move? settings) from to)
        red (if (:game-settings/owner-first-move? settings) to from)
        board (:game-settings/board settings)]
    { :db/id -1
      :game/blue blue
      :game/red red
      :game/owner from
      :game/settings settings
      :game/turn blue }))

(defn start [invite]
  (let [{ from :invite/from
          to :invite/to } invite
        game (make-game invite)]
    (d/transact! db (concat
                      [{ :db/id from
                         :user/playing? true }
                       { :db/id to
                         :user/playing? true }
                       { :db.fn/retractEntity (:db/id (invite/get-by-user-eid from)) }
                       { :db.fn/retractEntity (:db/id (invite/get-by-user-eid to)) }
                       game]
                       (create-game-board-cells game)))))

(defn get [owner-username]
  (let [owner-eid (user/get-eid owner-username)]
    (db/entity-by-av :game/owner owner-username)))

(defn get-user-color [game username]
  (let [settings (game-settings/get-by-game game)
        user-eid (user/get-eid username)]
    (if (and (= (:game/owner game) user-eid)
             (:game-settings/owner-first-move? settings))
      :blue
      :red)))

;; (defn make-move [game username src-cell dst-cell]
;;   (when (hex/is-jump? src-cell dst-cell)
;;     (clear-cell src-cell))
;;   (occupy-cell dst-cell))

(defn movements-available? [game]
;; (movements-available? game :red)
;; (movements-available? game :blue)
;; TODO
  true)

