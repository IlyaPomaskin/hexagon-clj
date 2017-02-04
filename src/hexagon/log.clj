(ns hexagon.log
  (:use compojure.core)
  (:require [clojure.string :as string]))

(defn now []
  (.format (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss") (new java.util.Date)))

(defn log [channel level]
  (fn [& args]
    (println (format "[%s] [%s] %s: %s" (now) channel level (string/join " " args)))))

(def ws-info (log "ws" "INFO"))
(def ws-error (log "ws" "ERR"))
(def user-info (log "user" "INFO"))
(def user-error (log "user" "ERR"))
(def user-debug (log "user" "DEBUG"))
(def user-debug (fn [& args] nil))
