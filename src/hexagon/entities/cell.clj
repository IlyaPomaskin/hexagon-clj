(ns hexagon.entities.cell
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]))

;;
(defn create-cells [size]
  (->>
    (range 0 size)
    (map
      (fn [x]
        (map
          (fn [y] { :x x :y y })
          (range 0 size))))
    flatten
    (map
      (fn [{ x :x y :y}]
        { :cell/x x
          :cell/y y
          :cell/game 123 }))))

;; (d/transact! db (create-cells 10))
;;

(defn get-board [game]
  (->>
    game
    :db/id
    (d/q
      '[:find [(pull ?e ["*"]) ...]
        :in $ ?game
        :where
        [?e ?a ?v]
        [?e :cell/game ?game]]
      @db)
    (group-by :cell/x)))

(defn is-valid-move? [game username src-cell dst-cell]
;;   (let [map (:game/map game)
;;         user-color (get-user-color game username)]
;;     (and
;;       (hex/user-own-cell? map user-color src-cell)
;;       (hex/cell-in-range? map src-cell dst-cell)
;;       (hex/cell-is-empty? map dst-cell))))
  true)

(defn is-offset? [x]
  (odd? x))

(defn is-offset-cell? [cell]
  (is-offset? (:x cell)))

(def offset-neighbours
  [[-1 -1] [0 -1] [1 -1]
   [-1  0] [0  1] [1  0]])

(def non-offset-neighbours
  [[-1 0] [0 -1] [1 0]
   [-1 1] [0  1] [1 1]])

;; (get-offsets-by-cell cell1)

;; (defn is-neighbour? [cell1 cell2]
;;   (let [offsets (get-offsets-by-cell cell1)]))

(defn get-neighbours [board-cells main-cell]
  (reduce
    (fn [neighbour-cells cell]
      (if (is-neighbour? main-cell cell)
        (conj neighbour-cells cell)
        neighbour-cells))
    #{}
    board-cells))

(defn distance-between-cells [src-cell dst-cell]
  ;;TODO
  true)

(defn is-jump? [src-cell dst-cell]
  (= (distance-between-cells src-cell dst-cell)
     2))

(defn get-cell [map x y]
  (first (filterv (fn [{ cell-x :x
                         cell-y :y }]
                    (and (= cell-x x) (= cell-y y)))
                  map)))

(defn user-own-cell? [map color { x :x y :y }]
  (= (:owner (get-cell map x y))
     color))

(defn cell-in-range? [map src-cell dst-cell]
  (<= (distance-between-cells src-cell dst-cell)
      2))

(defn cell-is-empty? [map { x :x y :y }]
  (= (:owner (get-cell map x y))
     :none))

(defn autofill-board [map]
  true)
