(ns hexagon.entities.cell
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.entities.game :as game]
            [hexagon.entities.user :as user]))

;; tmp utils

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

;; conversion

(defn even-q->cube [cell]
  (let [{ col :cell/x
          row :cell/y } cell
        x col
        z (- row (/ (+ col (bit-and col 1)) 2))
        y (- (- x) z)]
    { :cell/x x
      :cell/z z
      :cell/y y }))

(defn cube->even-q [cell]
  (let [{ x :cell/x
          y :cell/y
          z :cell/z } cell
        col x
        row (+ z (/ (+ x (bit-and x 1)) 2))]
    { :cell/x col
      :cell/y row }))

;; distance

(defn distance [src-cell dst-cell]
  (let [{ src-x :cell/x
          src-y :cell/y
          src-z :cell/z } (even-q->cube src-cell)
        { dst-x :cell/x
          dst-y :cell/y
          dst-z :cell/z } (even-q->cube dst-cell)]
    (/ (+ (Math/abs (- src-x dst-x))
          (Math/abs (- src-y dst-y))
          (Math/abs (- src-z dst-z)))
        2)))

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
  (<= (distance src-cell dst-cell)
      2))

(defn is-valid-move? [game-board username src-cell-coords dst-cell-coords]
  (let [src-cell (get-cell src-cell-coords)
        dst-cell (get-cell dst-cell-coords)]
    (and
      (is-available-cell? src-cell)
      (is-available-cell? dst-cell)
      (user-own-cell? src-cell username)
      (cell-is-empty? dst-cell)
      (cell-in-range? src-cell dst-cell))))
