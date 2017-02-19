(ns hexagon.entities.timeout
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]))

(defonce initial-timeouts
  (d/transact! db [{ :timeout/seconds 60 }
                   { :timeout/seconds 90 }
                   { :timeout/seconds 120 }]))

(def default
  (db/eid-by-av :timeout/seconds 90))

(defn get [timeout]
  (db/entity-by-av :timeout/seconds timeout))

(defn exists? [timeout]
  (some? (get timeout)))
