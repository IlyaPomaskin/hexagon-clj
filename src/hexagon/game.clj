(ns hexagon.game
  (:use compojure.core
        [org.httpkit.server :only (send!)])
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as string]
            [cheshire.core :as json]
            [hexagon.log :as log]
            [hexagon.config :as config]
            [hexagon.entities :as entities]))

(defonce users
  (atom {}))

(defn send-msg [type username & { :keys [error payload]
                                  :or {error nil, payload nil} }]
  (let [base { :type type
               :timestamp (System/currentTimeMillis)
               :status (if (nil? error) "OK" "ERR") }
        msg (if (nil? error)
              (assoc base :payload payload)
              (assoc base :error error))
        channel (get-in @users [username :channel])
        json (json/encode msg { :pretty config/PRETTY-PRINT })]
    (log/user-debug username "send" json)
    (send! channel json)))

(defn add-user [username channel]
  (log/user-info username "added")
  (swap! users assoc username (entities/create-user username channel)))

(defn delete-user [username]
  (log/user-info username "deleted")
  (swap! users dissoc username))

(defn get-users-list [{ username :username }]
  (send-msg "users-list" username :payload (keys @users)))

(defn get-boards [{ username :username }]
  (send-msg "boards" username :payload entities/available-boards))

(defn invite [{ src-username :username
                dst-username :dst
                game-settings :game-settings }]
  (swap! users assoc-in [src-username :invites dst-username] { :game-settings game-settings
                                                               :username src-username })
  (send-msg "invite" dst-username :payload { :src src-username
                                             :game-settings game-settings }))

(defn send-invite [msg]
  (let [{ src-username :username
          dst-username :dst } msg
        game-settings (entities/create-game-settings (:game-settings msg))
        src-send-err (partial send-msg "send-invite" src-username :error)]
    (cond
      (nil? dst-username) (src-send-err "no username")
      (not (contains? @users dst-username)) (src-send-err  "user not found")
      (= src-username dst-username) (src-send-err  "wrong user")
      (true? (get-in @users [dst-username :is-playing])) (src-send-err  "user already playing")
      ;; limit invites?
      :else (invite (assoc msg :game-settings game-settings)))))

(defn dispatch-message [msg]
  (log/user-debug (:username msg) "receive" msg)
  (match [msg]
         [{ :type "get-users-list" }] (get-users-list msg)
         [{ :type "get-boards" }] (get-boards msg)
         [{ :type "send-invite" }] (send-invite msg)
;;          [{ :type "confirm-proposal" }] (confirm-proposal msg)
;;          [{ :type "cancel-proposal" }] (cancel-proposal msg)
;;          [{ :type "start-game" }] (start-game msg)
;;          [{ :type "make-move" }] (make-move msg)
;;          [{ :type "finish-game" }] (finish-game msg)
         :else (log/user-error (:username msg) "unknown message" (:type msg))))
