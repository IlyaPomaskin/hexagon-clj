(ns hexagon.config)

(def PRETTY-PRINT true)

(defonce available-boards
  { "classic" { :q 1 :w 2 }
    "modern" { :q 3 :w 4 } })

(defonce timeouts
  #{60 90 120 180 300})

(defonce default-board
  (first (keys available-boards)))

(defonce default-timeout
  (first timeouts))
