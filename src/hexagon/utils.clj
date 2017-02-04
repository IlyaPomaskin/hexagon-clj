(ns hexagon.utils
  (:use compojure.core)
  (:require [clojure.string :as string]))

(defn is-empty-string [string]
  (and (string? string) (string/blank? string)))

(defn string-to-keyword [string]
  (if (or (is-empty-string string) (nil? string))
    nil
    (keyword (str string))))
