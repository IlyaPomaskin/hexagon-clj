(ns hexagon.entities
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]))

;; boards

(d/transact! db [{ :board/name "classic"
                   :board/map [{ :x 1
                                 :y 2 }]}
                 { :board/name "modern"
                   :board/map [{ :x 2
                                 :y 1 }]}])

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

;; (defn get-in-users
;;   ([path] (get-in @users path))
;;   ([path default-value] (get-in @users path default-value)))

;; (defn assoc-in-users [path value]
;;   (swap! users assoc-in path value))

;; (defn update-in-users [path fn]
;;   (swap! users update-in path fn))

(defn get-user [username]
  (db/entity-by-av :user/name username))

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

;; invites

(defn add-invite [from to settings]
  (d/transact! db [(assoc (create-game-settings settings) :db/id -1)
                   { :invite/to to
                     :invite/from from
                     :invite/settings -1 }]))

(defn invite-exists? [from to]
  (some? (get-invite from to)))

(defn invite-from-user-exists? [from]
  (some? (d/q '[:find ?e .
                :where
                [?e :invite/from (db/eid-by-av :user/name from)]] @db)))

(defn get-invite [from to]
  (d/q '[:find ?e .
         :where
         [?e :invite/from (db/eid-by-av :user/name from)]
         [?e :invite/to (db/eid-by-av :user/name to)]] @db))

;; game-settings

(defn create-game-settings [settings]
  (let [board-eid (-> settings :board get-board :db/id (or default-board))
        timeout-eid (-> settings :timeout get-timeout :db/id (or default-timeout))
        src-first-move? (boolean (:src-first-move settings))]
    { :game-settings/board board-eid
      :game-settings/timeout board-eid
      :game-settings/src-first-move? src-first-move? }))
