(ns hexagon.game
  (:use compojure.core
        [org.httpkit.server :only (send!)])
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as string]
            [cheshire.core :as json]
            [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.log :as log]
            [hexagon.config :as config]
            [hexagon.entities.board :as board]
            [hexagon.entities.user :as user]
            [hexagon.entities.invite :as invite]
            [hexagon.entities.game :as game]
            [hexagon.entities.cell :as cell]))

(defn send-msg [type username & { :keys [error payload]
                                  :or {error nil, payload nil} }]
  (let [base { :type type
               :timestamp (System/currentTimeMillis)
               ;; :parent-msg-id parent-msg-id
               :status (if (nil? error) "OK" "ERR") }
        msg (if (nil? error)
              (assoc base :payload payload)
              (assoc base :error error))
        channel (:user/channel (user/get username))
        json (json/encode msg { :pretty config/PRETTY-PRINT })]
    (log/ws-debug username "send" json)
    (send! channel json)))

(defn add-user [username channel]
  (log/game-info username "enter")
  (user/add username channel)
  (let [eid (db/eid-by-av :user/name username)
        filter-datoms (partial filterv (get @user/filters eid))]
    (send-msg "datoms" username :payload (-> @db (d/datoms :aevt) filter-datoms pr-str))
    (d/listen! db eid #(if-let [datoms (-> %1 :tx-data filter-datoms pr-str)]
                         (send-msg "datoms" username :payload datoms)))))

(defn delete-user [username]
  (let [user-eid (db/eid-by-av :user/name username)]
    (log/game-info username "exit")
    (d/unlisten! db user-eid)
    (user/delete username)))

(defn get-users-list [{ username :username }]
  (send-msg "users-list" username :payload (user/get-usernames)))

(defn get-boards [{ username :username }]
  (send-msg "boards" username :payload board/get-boards))

(defn invite [{ src-username :username
                dst-username :to
                game-settings :game-settings }]
  (invite/add src-username dst-username game-settings)
  (send-msg "invite" dst-username :payload { :from src-username
                                             :game-settings game-settings }))

(defn send-invite [msg]
  (let [{ src-username :username
          dst-username :to
          game-settings :game-settings } msg
        src-send-err (partial send-msg "send-invite" src-username :error)]
    (cond
      (not (user/exists? dst-username)) (src-send-err  "user not found")
      (= src-username dst-username) (src-send-err  "wrong user")
      (user/playing? dst-username) (src-send-err  "user already playing")
      (invite/get-by-username src-username) (src-send-err "invite already sent")
      :else (invite msg))))

(defn start-game [invite]
  (let [serialized-invite (invite/serialize invite)]
    (game/start invite)
    (send-msg "start-game" (:user/name (:invite/to invite)) :payload serialized-invite)
    (send-msg "start-game" (:user/name (:invite/from invite)) :payload serialized-invite)
    (log/game-info "start-game" serialized-invite)))

(defn accept-invite [msg]
  (let [{ src-username :username
          dst-username :from } msg
        src-send-err (partial send-msg "accept-invite" src-username :error)]
    (cond
      (not (user/exists? dst-username)) (src-send-err  "user not found")
      (= src-username dst-username) (src-send-err  "wrong user")
      (false? (invite/exists? dst-username src-username)) (src-send-err "user canceled invite")
      :else (start-game (invite/get dst-username src-username)))))

(defn next-player-turn [game]
  (game/switch-turn game)
  (log/game-info "next-player-turn"))

(defn win [game]
  (let [[winner-eid loser-eid] (game/get-results game)
        winner-username (:user/name (user/get-by-eid winner-eid))
        loser-username (:user/name (user/get-by-eid loser-eid))]
    (send-msg "win" winner-username)
    (send-msg "lose" loser-username)
    (log/game-info "game result" "winner" winner-username "loser" loser-username)))

(defn make-move [msg]
  (let [{ username :username
          src-cell :src-cell
          dst-cell :dst-cell } msg
        game (game/get username)
        game-board (cell/get-board game)
        src-send-err (partial send-msg "make-move" username :error)]
    (cond
      (nil? game) (src-send-err "game not found")
      (false? (game/is-user-turn? game username)) (src-send-err "wrong turn")
      (nil? (cell/is-valid-move? game-board username src-cell dst-cell)) (src-send-err "invalid move")
      :else (do
              (game/move game username src-cell dst-cell)
              ;; TODO
              ;; (send-current-board (:game/red game))
              ;; (send-current-board (:game/blue game))
              (if (game/movements-available? game)
                (next-player-turn game)
                (win game))))))

(defn dispatch-message [msg]
  (log/user-debug (:username msg) "receive" msg)
  (match [msg]
         ;; TODO check user state before dispatch
         [{ :type "get-users-list" }] (get-users-list msg)
         [{ :type "get-boards" }] (get-boards msg)
         [{ :type "send-invite" }] (send-invite msg)
         ;; [{ :type "cancel-invite" }] (cancel-invite msg)
         [{ :type "accept-invite" }] (accept-invite msg)
         [{ :type "make-move" }] (make-move msg)
         ;; [{ :type "surrender" }] (surrender msg)
         :else (log/user-error (:username msg) "unknown message" (:type msg))))
