(ns hexagon.hex)

(defn is-jump? [src-cell dst-cell]
  true)

(defn get-cell [map x y]
  (first (filterv (fn [{ cell-x :x
                         cell-y :y }]
                    (and (= cell-x x) (= cell-y y)))
                  map)))

(defn user-own-cell? [map color { x :x y :y }]
  (= (:owner (get-cell map x y))
     color))

(defn cell-in-range? [map { src-x :x src-y :y } { dst-x :x dst-y :y }]
  ;; TODO
  true)

(defn cell-is-empty? [map { x :x y :y }]
  (= (:owner (get-cell map x y))
     :none))

(defn autofill-board [map]
  true)
