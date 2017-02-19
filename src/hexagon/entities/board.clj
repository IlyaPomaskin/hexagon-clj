(ns hexagon.entities.board
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]))

(defonce initial-boards
  (d/transact! db [{ :board/name "classic"
                     :board/map [{ :cell/x 0
                                   :cell/y 0
                                   :cell/type :normal
                                   :cell/owner nil }
                                 { :cell/x 1
                                   :cell/y 0
                                   :cell/type :normal
                                   :cell/owner :blue }
                                 { :cell/x 1
                                   :cell/y 1
                                   :cell/type :normal
                                   :cell/owner :red }
                                 { :cell/x 1
                                   :cell/y 1
                                   :cell/type nil }]}
                   { :board/name "modern"
                     :board/map [{ :cell/x 2
                                   :cell/y 1
                                   :cell/type :normal
                                   :cell/owner nil }]}]))

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
