(ns hexagon.entities.user
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.entities.board :as board]
            [hexagon.entities.timeout :as timeout]))

(defn get [username]
  (db/entity-by-av :user/name username))

(defn get-eid [username]
  (db/eid-by-av :user/name username))

(defn get-by-eid [eid]
  (db/entity-by-eid eid))

(defn add [username channel]
  (d/transact! db [{ :user/name username
                     :user/channel channel
                     :user/playing? false }]))

(defn delete [username]
  (db/retract-by-av :user/name username))

(defn exists? [username]
  (some? (db/eid-by-av :user/name username)))

(defn get-usernames []
  (d/q '[:find [?name ...]
         :where [_ :user/name ?name]] @db))

(defn playing? [username]
  (:playing? (db/entity-by-av :user/name username)))
