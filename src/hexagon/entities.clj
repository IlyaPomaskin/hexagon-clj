(ns hexagon.entities)

;; boards
(defonce available-boards
  { "classic" { :q 1 :w 2 }
    "modern" { :q 3 :w 4 } })

(defonce default-board
  (first (keys available-boards)))

;;timeouts

(defonce timeouts
  #{60 90 120 180 300})

(defonce default-timeout
  (first timeouts))

;; user

(defn create-user [username channel]
  { :username username
    :channel channel
    :is-playing false
    :invites {} })

;; users

(defonce users
  (atom {}))

(defn get-in-users
  ([path] (get-in @users path))
  ([path default-value] (get-in @users path default-value)))

(defn assoc-in-users [path value]
  (swap! users assoc-in path value))

(defn update-in-users [path fn]
  (swap! users update-in path fn))

(defn add-user [username channel]
  (assoc-in-users [username] (create-user username channel)))

(defn delete-user [username]
  (swap! users dissoc username))

(defn user-exists? [username]
  (contains? @users username))

(defn get-usernames []
  (keys @users))

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
