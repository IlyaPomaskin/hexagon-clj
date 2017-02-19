(ns hexagon.entities.board
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.hex :as hex]))

;; boards

(d/transact! db [{ :board/name "classic"
                   :board/map [{ :x 1
                                 :y 2
                                 :type :normal
                                 :owner nil }]}
                 { :board/name "modern"
                   :board/map [{ :x 2
                                 :y 1
                                 :owner nil }]}])

(def default-board (db/eid-by-av :board/name "classic"))

(defn get-board [name]
  (db/entity-by-av :board/name name))

(defn board-exists? [board]
  (some? (get-board board)))

;; TODO return map instead of vec
(defn get-boards []
  (d/q '[:find ?name ?map
         :where
         [_ :board/name ?name]
         [_ :board/map ?map]] @db))
