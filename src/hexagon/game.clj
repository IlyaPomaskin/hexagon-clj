(ns hexagon.game
  (:use compojure.core
        [org.httpkit.server :only (send!)])
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as string]
            [cheshire.core :as json]
            [hexagon.log :as log]))

(def PRETTY-PRINT true)

(defonce users
  (atom {}))

(defonce available-boards
  { :classic { :q 1 :w 2 }
    :modern { :q 3 :w 4 } })

(defn send-msg [type username & { :keys [error payload]
                                  :or {error nil, payload nil} }]
  (let [base { :type type
               :timestamp (System/currentTimeMillis)
               :status (if (nil? error) "OK" "ERR") }
        msg (if (nil? error)
              (assoc base :payload payload)
              (assoc base :error error))
        channel (get-in @users [username :channel])
        json (json/encode msg { :pretty PRETTY-PRINT })]
    (log/user-debug username "send" json)
    (send! channel json)))

(defn create-user [username channel]
  (log/user-info username "added")
  (swap! users assoc username
         { :username username
           :channel channel
           :is-playing false }))

(defn remove-user [username]
  (log/user-info username "deleted")
  (swap! users dissoc username))

(defn get-users-list [{ username :username }]
  (send-msg "users-list" username :payload (keys @users)))

(defn get-boards [{ username :username }]
  (send-msg "boards" username :payload available-boards))

(defn get-valid-boards [boards]
  (->> boards
       (map #(keyword %1))
       (filter #(contains? available-boards %1))))

(defn propose-game [{ src-username :username dst-username :to boards :boards }]
  (let [valid-boards (get-valid-boards boards)]
    (cond
      (nil? dst-username) (send-msg "propose-game" src-username :error "no username")
      (not (contains? @users dst-username)) (send-msg "propose-game" src-username :error "user not found")
      (empty? valid-boards) (send-msg "propose-game" src-username :error "no boards")
      (= src-username dst-username) (send-msg "propose-game" src-username :error "wrong user")
      (true? (get-in @users [dst-username :is-playing])) (send-msg "propose-game" src-username :error "user already playing")
      ;; limit proposals?
      :else (send-msg "game-proposal" dst-username :payload { :from src-username :boards valid-boards }))))

(defn dispatch-message [msg]
  (log/user-debug (:username msg) "receive" msg)
  (match [msg]
         [{ :type "get-users-list" }] (get-users-list msg)
         [{ :type "get-boards" }] (get-boards msg)
         [{ :type "propose-game" }] (propose-game msg)
;;          [{ :type "confirm-proposal" }] (confirm-proposal msg)
;;          [{ :type "cancel-proposal" }] (cancel-proposal msg)
;;          [{ :type "start-game" }] (start-game msg)
;;          [{ :type "make-move" }] (make-move msg)
;;          [{ :type "finish-game" }] (finish-game msg)
         :else (log/user-error (:username msg) "unknown message" (:type msg))))
