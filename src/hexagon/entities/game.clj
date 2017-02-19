(ns hexagon.entities.game
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.entities.board :as board]
            [hexagon.entities.timeout :as timeout]
            [hexagon.entities.invite :as invite]
            [hexagon.entities.user :as user]
            [hexagon.entities.game-settings :as game-settings]
            [hexagon.entities.cell :as cell]))

(defn set-cell-owner [cell game]
  (if (some? (:cell/owner cell))
    (assoc cell :cell/owner (if (= (:cell/owner cell) :red)
                              (:game/red game)
                              (:game/blue game)))
    (dissoc cell :cell/owner)))

(defn board-cell->game-cell [game cell]
  (-> cell
      (set-cell-owner game)
      (assoc :cell/game (:db/id game))))

(defn create-game-board [game invite]
  (->> invite
       :invite/settings
       :game-settings/board
       :board/map
       (mapv (partial board-cell->game-cell game))))

(defn make-game [invite]
  (let [{ from :invite/from
          to :invite/to
          settings :invite/settings } invite
        blue-eid (:db/id (if (:game-settings/owner-first-move? settings) from to))
        red-eid (:db/id (if (:game-settings/owner-first-move? settings) to from))]
    { :db/id -1
      :game/blue blue-eid
      :game/red red-eid
      :game/owner (:db/id from)
      :game/settings (:db/id settings)
      :game/turn blue-eid }))

(defn start [invite]
  (let [from-eid (:db/id (:invite/from invite))
        to-eid (:db/id (:invite/to invite))
        game (make-game invite)
        invites-for-deletion (into #{} (clojure.set/union (invite/get-eids-by-user-eid from-eid)
                                                          (invite/get-eids-by-user-eid to-eid)))]
    (d/transact! db (concat
                      [{ :db/id from-eid
                         :user/playing? true }
                       { :db/id to-eid
                         :user/playing? true }
                       game]
                      (mapv #(vec [:db.fn/retractEntity %1]) invites-for-deletion)
                      (create-game-board game invite)))))

(defn get [username]
  (let [user-eid (user/get-eid username)]
    (or (db/entity-by-av :game/red user-eid)
        (db/entity-by-av :game/blue user-eid))))

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
    ;; TODO autofill board
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

(defn switch-turn [{ game-eid :db/id
                     red :game/red
                     blue :game/blue
                     turn :game/turn }]
  (d/transact! db { :db/id game-eid
                    :game/turn (if (= turn red) blue red) }))

(defn get-cells-count-by-user [game-eid user-eid]
  (d/q '[:find (count ?e) .
         :in $ ?game-eid ?user-eid
         :where
         [?e :cell/owner ?user-eid]
         [?e :cell/game ?game-eid]] @db game-eid user-eid))

(defn get-results [{ game-eid :db/id
                     red :game/red
                     blue :game/blue }]
  (let [red-count (get-cells-count-by-user game-eid red)
        blue-count (get-cells-count-by-user game-eid blue)]
    (if (> blue-count red-count)
      [red blue]
      [blue red])))

;; TODO delete entries after game
