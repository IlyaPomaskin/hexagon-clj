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

;; TODO return map instead of vec
(defn get-boards []
  (d/q '[:find ?name ?map
         :where
         [_ :board/name ?name]
         [_ :board/map ?map]] @db))

(defn board-exists [board]
  (not (nil? (db/eid-by-av :board/name board))))

;;timeouts

(d/transact! db [{ :timeout/seconds 60 }
                 { :timeout/seconds 90 }
                 { :timeout/seconds 120 }])

(def default-timeout (db/eid-by-av :timeout/seconds 90))

(defn timeout-exists [timeout]
  (not (nil? (db/eid-by-av :timeout/seconds timeout))))

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
  (not (nil? (db/eid-by-av :user/name username))))

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
  (not (nil? (get-invite from to))))

(defn get-invite [from to]
  (d/q '[:find ?e .
           :where
           [?e :invite/from (db/eid-by-av :user/name src)]
           [?e :invite/to (db/eid-by-av :user/name dst)]] @db))

;; game-settings

(defn create-game-settings [{ board :board
                              timeout :timeout
                              src-first-move? :is-src-first }]
  { :game-settings/board (if-not (board-exists board) default-board board)
    :game-settings/timeout (if-not (timeout-exists timeout) default-timeout timeout)
    :game-settings/src-first-move? (boolean src-first-move?) })
