(ns hexagon.game
  (:use compojure.core
        [org.httpkit.server :only (send!)])
  (:require [clojure.core.match :refer [match]]
            [cheshire.core :as json]
            [hexagon.log :as log]))

(defonce users (atom {}))

(defonce available-boards
  { :classic { :q 1 :w 2 }
    :modern { :q 3 :w 4 } })

(defn send-msg [username msg]
  (log/user-debug username "send" msg)
  (send! ;; Убрать send! в ws.clj
    (get-in @users [username :channel])
    (json/encode msg)))

(defn create-user [username channel]
  (log/user-info username "added")
  (swap! users assoc username { :username username :channel channel }))

(defn remove-user [username]
  (log/user-info username "deleted")
  (swap! users dissoc username))

(defn get-users-list [{ username :username }]
  (send-msg username (keys @users)))

(defn get-boards [{ username :username }]
  (send-msg username { :boards available-boards }))

(defn dispatch-message [username msg]
  (log/user-debug username "receive" msg)
(defn dispatch-message [msg]
  (log/user-debug (:username msg) "receive" msg)
  (match [msg]
         [{ :type "get-users-list" }] (get-users-list msg)
         [{ :type "get-boards" }] (get-boards msg)
         :else (log/user-error (:username msg) "unknown message" (:type msg))))
