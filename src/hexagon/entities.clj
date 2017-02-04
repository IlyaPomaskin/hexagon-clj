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

;; users


;; user

(defn create-user [username channel]
  { :username username
    :channel channel
    :is-playing false
    :invites {} })

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
