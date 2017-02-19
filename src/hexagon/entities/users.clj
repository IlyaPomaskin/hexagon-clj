(ns hexagon.entities.users
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.hex :as hex]
            [hexagon.entities.boards :as boards]
            [hexagon.entities.timeouts :as timeouts]))

;; users

(defn get-user [username]
  (db/entity-by-av :user/name username))

(defn get-user-eid [username]
  (db/eid-by-av :user/name username))

(defn get-user-by-eid [eid]
  (db/entity-by-eid eid))

(defn add-user [username channel]
  (d/transact! db [{ :user/name username
                     :user/channel channel
                     :user/playing? false }]))

(defn delete-user [username]
  (db/retract-by-av :user/name username))

(defn user-exists? [username]
  (some? (db/eid-by-av :user/name username)))

(defn get-usernames []
  (d/q '[:find [?name ...]
         :where [_ :user/name ?name]] @db))

(defn user-playing? [username]
  (:playing? (db/entity-by-av :user/name username)))
