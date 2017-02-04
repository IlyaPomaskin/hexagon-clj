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

(defn dispatch-message [username msg]
  (log/user-debug username "receive" msg)
(defn dispatch-message [msg]
  (log/user-debug (:username msg) "receive" msg)
  (match [msg]
         [{ :type "get-users-list" }] (get-users-list msg)
         [{ :type "get-boards" }] (get-boards msg)
         :else (log/user-error (:username msg) "unknown message" (:type msg))))
