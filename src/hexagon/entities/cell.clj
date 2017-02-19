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
          :cell/type :normal
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

(defn get-cell-by-coords [game-board x y]
  (->
    game-board
    (clojure.core/get x [])
    (clojure.core/get y)))

(defn get-cell [game-board cell]
  (get-cell-by-coords game-board (:cell/x cell) (:cell/y cell)))

(defn is-available-cell? [cell]
  (= (:cell/type cell)
     :normal))

(defn user-own-cell? [cell username]
  (= (:cell/owner cell)
     (user/get-eid username)))

(defn cell-is-empty? [cell]
  (nil? (:cell/owner cell)))

(defn cell-in-range? [src-cell dst-cell]
  ;; TODO
  true)

(defn is-valid-move? [game username src-cell-coords dst-cell-coords]
  (let [game-board (get-board game)
        src-cell (get-cell src-cell-coords)
        dst-cell (get-cell dst-cell-coords)]
    (and
      (is-available-cell? src-cell)
      (is-available-cell? dst-cell)
      (user-own-cell? src-cell username)
      (cell-is-empty? dst-cell)
      (cell-in-range? src-cell dst-cell))))

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
