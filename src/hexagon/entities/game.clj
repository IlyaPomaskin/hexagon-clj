(ns hexagon.entities.game
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.entities.board :as board]
            [hexagon.entities.timeout :as timeout]
            [hexagon.entities.invite :as invite]
            [hexagon.entities.user :as user]
            [hexagon.entities.game-settings :as game-settings]
            [hexagon.entities.cell :as cell]))

(defn create-game-board [game]
  (->>
    game
    :game/settings
    (d/q '[:find ?map .
           :in $ ?eid
           :where
           [?board :board/map ?map]
           [?eid :game-settings/board ?board]] @db)
    (mapv #(merge %1
                  { :cell/owner (case (:owner %1)
                                  :red (:game/red game)
                                  :blue (:game/blue game)
                                  nil)
                    :cell/game (:db/id game) }))))

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
                       (create-game-board game)))))

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

(defn move [game username src-cell-coords dst-cell-coords]
  (let [changes (atom #{})
        src-cell (cell/get-cell src-cell-coords)
        dst-cell (cell/get-cell dst-cell-coords)
        dst-w-neighbours (conj (cell/get-neighbours dst-cell)
                               dst-cell)
        next-dst (cell/occupy-cells dst-w-neighbours (user/get-eid username))
        game-board (atom nil)]
    (when (cell/is-jump? src-cell dst-cell)
      (swap! changes conj (cell/clear-cell src-cell)))
    (swap! changes clojure.set/union (cell/occupy-cells (user/get-eid username) dst-w-neighbours))
    (d/transact! db @changes)
    ;; TODO
    ;; (reset! game-board (cell/get-board game))
    ;; (when-not (movements-available? @game-board)
    ;;   (reset! changes (fill-board @game-board)))
    ;; (d/transact! db @changes)
    ))

(defn movements-available? [game]
;; (movements-available? game :red)
;; (movements-available? game :blue)
;; TODO
  true)

