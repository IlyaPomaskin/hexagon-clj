(ns hexagon.game
  (:use compojure.core
        [org.httpkit.server :only (send!)])
  (:require [clojure.core.match :refer [match]]
            [hexagon.log :as log]))

(defonce users (atom {}))

(defn send-msg [username msg]
  (log/user-debug username "send" msg)
  (send!
    (get-in @users [username :channel])
    (json/encode msg)))

(defn create-user [username channel]
  (log/user-info username "added")
  (swap! users assoc username { :username username :channel channel }))

(defn remove-user [username]
  (log/user-info username "deleted")
  (swap! users dissoc username))

(defn get-users-list [username]
  (send-msg username { :smth 123 }))

(defn get-boards [username]
  (send-msg username { :boards [1 2 3]}))

(defn dispatch-message [username msg]
  (log/user-debug username "receive" msg)
  (match [msg]
         [{ :type "get-users-list" }] (get-users-list username)
         [{ :type "get-boards" }] (get-boards username)
         :else (log/user-error username "unknown message" (:type msg))))
