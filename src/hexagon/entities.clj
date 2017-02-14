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

;;timeouts

(d/transact! db [{ :timeout/seconds 60 }
                 { :timeout/seconds 90 }
                 { :timeout/seconds 120 }])

;; users

;; (defn get-in-users
;;   ([path] (get-in @users path))
;;   ([path default-value] (get-in @users path default-value)))

;; (defn assoc-in-users [path value]
;;   (swap! users assoc-in path value))

;; (defn update-in-users [path fn]
;;   (swap! users update-in path fn))

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

;; game-settings

(defn board-exists [board]
  (contains? available-boards board))

(defn timeout-exists [timeout]
  (contains? timeouts timeout))

(defn create-game-settings [{ board :board
                              timeout :timeout
                              is-src-first :is-src-first }]
  { :board (if-not (board-exists board) default-board board)
    :timeout (if-not (timeout-exists timeout) default-timeout timeout)
    :is-src-first (boolean is-src-first) })
