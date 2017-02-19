(ns hexagon.game
  (:use compojure.core
        [org.httpkit.server :only (send!)])
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as string]
            [cheshire.core :as json]
            [hexagon.log :as log]
            [hexagon.config :as config]
            [hexagon.entities.board :as board]
            [hexagon.entities.user :as user]
            [hexagon.entities.invite :as invite]
            [hexagon.entities.game :as game]
            [hexagon.entities.cell :as cell]))

(defn send-msg [type username parent-msg-id & { :keys [error payload]
                                  :or {error nil, payload nil} }]
  (let [base { :type type
               :timestamp (System/currentTimeMillis)
               :parent-msg-id parent-msg-id
               :status (if (nil? error) "OK" "ERR") }
        msg (if (nil? error)
              (assoc base :payload payload)
              (assoc base :error error))
        channel (:channel (user/get username))
        json (json/encode msg { :pretty config/PRETTY-PRINT })]
    (log/ws-debug username "send" json)
    (send! channel json)))

(defn add-user [username channel]
  (log/game-info username " enter")
  (user/add username channel))

(defn delete-user [username]
  (log/game-info username "exit")
  (user/delete username))

(defn get-users-list [{ username :username }]
  (send-msg "users-list" username :payload (user/get-usernames)))

(defn get-boards [{ username :username }]
  (send-msg "boards" username :payload board/get-boards))

(defn invite [{ src-username :username
                dst-username :dst
                game-settings :game-settings }]
  (invite/add src-username dst-username game-settings)
  (send-msg "invite" dst-username :payload { :src src-username
                                             :game-settings game-settings }))

(defn send-invite [msg]
  (let [{ src-username :username
          dst-username :dst
          game-settings :game-settings } msg
        src-send-err (partial send-msg "send-invite" src-username :error)]
    (cond
      (not (user/exists? dst-username)) (src-send-err  "user not found")
      (= src-username dst-username) (src-send-err  "wrong user")
      (user/playing? dst-username) (src-send-err  "user already playing")
      (invite/get-by-username src-username) (src-send-err "invite already sent")
      :else (invite src-username dst-username game-settings))))

(defn start-game [invite]
  (game/start invite)
  ;; TODO Remove keywords from 'invite' fieldnames
  (send-msg "start-game" (user/get-by-eid (:invite/to invite)) :payload invite)
  (send-msg "start-game" (user/get-by-eid (:invite/from invite)) :payload invite)
  (log/game-info "start-game" invite))

(defn accept-invite [msg]
  (let [{ src-username :username
          dst-username :dst } msg
        src-send-err (partial send-msg "accept-invite" src-username :error)]
    (cond
      (not (user/exists? dst-username)) (src-send-err  "user not found")
      (= src-username dst-username) (src-send-err  "wrong user")
      (invite/exists? dst-username src-username) (src-send-err "user canceled invite")
      :else (start-game (invite/get dst-username src-username)))))

(defn next-player-turn [game]
  (game/switch-turn)
  (log/game-info "next-player-turn"))

(defn win [game]
  (let [[winner-eid loser-eid] (game/get-winner game)
        winner-username (:user/name (user/get-by-eid winner-eid))
        loser-username (:user/name (user/get-by-eid loser-eid))]
    (send-msg "win" winner-username)
    (send-msg "lose" loser-username)
    (log/game-info "game result" "winner" winner-username "loser" loser-username)))

(defn make-move [msg]
  (let [{ username :username
          owner :game-owner
          src-cell :src-cell
          dst-cell :dst-cell } msg
        game (game/get owner)
        game-board (cell/get-board game)
        src-send-err (partial send-msg "make-move" username :error)]
    (cond
      (nil? game) (src-send-err "game not found")
      (cell/is-valid-move? game-board username src-cell dst-cell) (src-send-err "invalid move")
      :else (do
              (game/move game username src-cell dst-cell)
              (if (game/movements-available?)
                (next-player-turn game)
                (win game))))))

(defn dispatch-message [msg]
  (log/user-debug (:username msg) "receive" msg)
  (match [msg]
         ;; TODO check user state before dispatch
         [{ :type "get-users-list" }] (get-users-list msg)
         [{ :type "get-boards" }] (get-boards msg)
         [{ :type "send-invite" }] (send-invite msg)
         [{ :type "accept-invite" }] (accept-invite msg)
         [{ :type "make-move" }] (make-move msg)
;;          [{ :type "surrender" }] (surrender msg)
         :else (log/user-error (:username msg) "unknown message" (:type msg))))
