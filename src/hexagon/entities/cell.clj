(ns hexagon.entities.cell
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.entities.user :as user]
            [hexagon.log :as log]))

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
          :cell/type :normal }))
    vec))

;; (-> (create-cells 5)
;;     (assoc-in [0 :cell/owner] :red)
;;     (assoc-in [(- (* 5 5) 1) :cell/owner] :blue)
;;     clojure.pprint/pprint)

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

;; neighbours

(def offset-neighbours
  [[-1 -1] [0 -1] [1 -1]
   [-1  0] [0  1] [1  0]])

(def non-offset-neighbours
  [[-1 0] [0 -1] [1 0]
   [-1 1] [0  1] [1 1]])

(defn is-offset-cell? [cell]
  (odd? (:cell/x cell)))

(defn get-neighbours [game-board cell]
  (let [{ x :cell/x
          y :cell/y } cell]
    (reduce
      (fn [neighbours [offset-x offset-y]]
        (let [offset-cell (get-cell-by-coords game-board (+ x offset-x) (+ y offset-y))]
          (if (is-available-cell? offset-cell)
            (conj neighbours offset-cell)
            neighbours)))
      #{}
      (if (is-offset-cell? cell)
        offset-neighbours
        non-offset-neighbours))))

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
    (sort-by :cell/y)
    (group-by :cell/x)
    (into (sorted-map))))

(defn get-cell-by-coords [game-board x y]
  (->
    game-board
    (clojure.core/get x [])
    (clojure.core/get y)))

(defn get-cell [game-board cell]
  (get-cell-by-coords game-board (:x cell) (:y cell)))

(defn is-available-cell? [cell]
  (= (:cell/type cell)
     :normal))

(defn user-own-cell? [cell username]
  (= (:db/id (:cell/owner cell))
     (user/get-eid username)))

(defn cell-is-empty? [cell]
  (nil? (:cell/owner cell)))

(defn cell-in-range? [src-cell dst-cell]
  (<= (distance src-cell dst-cell)
      2))

(defn is-jump? [src-cell dst-cell]
  (= (distance src-cell dst-cell)
     2))

(defn is-valid-move? [game-board username src-cell-coords dst-cell-coords]
  (let [src-cell (get-cell game-board src-cell-coords)
        dst-cell (get-cell game-board dst-cell-coords)]
    (cond
      (not (is-available-cell? src-cell)) (log/cell-error "is-valid-move?" "is-available-cell? src-cell")
      (not (is-available-cell? dst-cell)) (log/cell-error "is-valid-move?" "is-available-cell? dst-cell")
      (not (user-own-cell? src-cell username)) (log/cell-error "is-valid-move?" "user-own-cell?")
      (not (cell-is-empty? dst-cell)) (log/cell-error "is-valid-move?" "cell-is-empty?")
      (not (cell-in-range? src-cell dst-cell)) (log/cell-error "is-valid-move?" "cell-in-range?")
      :else true)))

(defn clear-cell [{ cell-eid :db/id }]
  { :db/id cell-eid
    :cell/owner nil })

(defn occupy-cells [user-eid cells]
  (->>
    cells
    (filterv #(and (some? (:cell/owner %1))
                   (not= (:cell/owner %1)
                         user-eid)))
    (mapv #(hash-map :db/id (:db/id %1)
                     :cell/owner user-eid))))
