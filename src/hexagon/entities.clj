(ns hexagon.entities
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.hex :as hex]))

;; boards

(d/transact! db [{ :board/name "classic"
                   :board/map [{ :x 1
                                 :y 2
                                 :type :normal
                                 :owner nil }]}
                 { :board/name "modern"
                   :board/map [{ :x 2
                                 :y 1
                                 :owner nil }]}])

(def default-board (db/eid-by-av :board/name "classic"))

(defn get-board [name]
  (db/entity-by-av :board/name name))

(defn board-exists? [board]
  (some? (get-board board)))

;; TODO return map instead of vec
(defn get-boards []
  (d/q '[:find ?name ?map
         :where
         [_ :board/name ?name]
         [_ :board/map ?map]] @db))

;;timeouts

(d/transact! db [{ :timeout/seconds 60 }
                 { :timeout/seconds 90 }
                 { :timeout/seconds 120 }])

(def default-timeout (db/eid-by-av :timeout/seconds 90))

(defn get-timeout [timeout]
  (db/entity-by-av :timeout/seconds timeout))

(defn timeout-exists? [timeout]
  (some? (get-timeout timeout)))

;; users

(defn get-user [username]
  (db/entity-by-av :user/name username))

(defn get-user-eid [username]
  (db/eid-by-av :user/name username))

(defn get-user-by-eid [eid]
  (db/entity-by-eid eid))

(defn add-user [username channel]
  (d/transact! db [{ :user/name username
                     :user/channel channel
                     :user/playing? false }]))

(defn delete-user [username]
  (db/retract-by-av :user/name username))

(defn user-exists? [username]
  (some? (db/eid-by-av :user/name username)))

(defn get-usernames []
  (d/q '[:find [?name ...]
         :where [_ :user/name ?name]] @db))

(defn user-playing? [username]
  (:playing? (db/entity-by-av :user/name username)))

;; game-settings

(defn create-game-settings [settings]
  (let [board-eid (-> settings :board get-board :db/id (or default-board))
        timeout-eid (-> settings :timeout get-timeout :db/id (or default-timeout))
        owner-first-move? (boolean (:owner-first-move settings))]
    { :game-settings/board board-eid
      :game-settings/timeout board-eid
      :game-settings/owner-first-move? owner-first-move? }))

;; invites

(defn add-invite [from to settings]
  (d/transact! db [(assoc (create-game-settings settings) :db/id -1)
                   { :invite/to (get-user-eid to)
                     :invite/from (get-user-eid from)
                     :invite/settings -1 }]))

(defn invite-exists? [from to]
  (some? (get-invite from to)))

(defn get-invite [from to]
  (d/q '[:find ?e .
         :in $ ?from ?to
         :where
         [?e :invite/from ?from]
         [?e :invite/to ?to]] @db (get-user-eid from) (get-user-eid to)))

(defn get-invite-by-user-eid [from]
  (db/entity-by-av :invite/from from))

(defn get-invite-by-username [from]
  (get-invite-by-user-eid (get-user-eid from)))

;; game

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

(defn make-move [game username src-cell dst-cell]
  ;; TODO
;;   (when (hex/is-jump? src-cell dst-cell)
;;     (clear-cell src-cell))
;;   (occupy-cell dst-cell))
)

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
