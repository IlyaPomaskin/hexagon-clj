(ns hexagon.entities.board
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]))

(defonce initial-boards
  (d/transact! db [{ :board/name "classic"
                     :board/map [{ :x 1
                                   :y 2
                                   :type :normal
                                   :owner nil }]}
                   { :board/name "modern"
                     :board/map [{ :x 2
                                   :y 1
                                   :type :normal
                                   :owner nil }]}]))

(def default
  (db/eid-by-av :board/name "classic"))

(defn get [name]
  (db/entity-by-av :board/name name))

(defn exists? [board]
  (some? (get board)))

;; TODO return map instead of vec
(defn get-boards []
  (d/q '[:find ?name ?map
         :where
         [_ :board/name ?name]
         [_ :board/map ?map]] @db))
