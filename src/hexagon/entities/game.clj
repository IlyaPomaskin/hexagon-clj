(ns hexagon.entities
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.hex :as hex]
            [hexagon.entities.boards :as boards]
            [hexagon.entities.timeouts :as timeouts]))

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

(defn start-game [invite]
  (let [{ from :invite/from
          to :invite/to } invite
        game (make-game invite)]
    (d/transact! db (concat
                      [{ :db/id from
                         :user/playing? true }
                       { :db/id to
                         :user/playing? true }
                       { :db.fn/retractEntity (:db/id (get-invite-by-user-eid from)) }
                       { :db.fn/retractEntity (:db/id (get-invite-by-user-eid to)) }
                       game]
                       (create-game-board-cells game)))))

(defn get-game [owner-username]
  (let [owner-eid (get-user-eid owner-username)]
    (db/entity-by-av :game/owner owner-username)))

(defn get-game-settings [game]
  (db/entity-by-eid (:game/settings game)))

(defn get-user-color [game username]
  (let [settings (get-game-settings game)
        user-eid (get-user-eid username)]
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

(defn is-valid-move? [game username src-cell dst-cell]
  (let [map (:game/map game)
        user-color (get-user-color game username)]
    (and
      (hex/user-own-cell? map user-color src-cell)
      (hex/cell-in-range? map src-cell dst-cell)
      (hex/cell-is-empty? map dst-cell))))
