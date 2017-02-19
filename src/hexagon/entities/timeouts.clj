(ns hexagon.entities.timeouts
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]))

;;timeouts

(d/transact! db [{ :timeout/seconds 60 }
                 { :timeout/seconds 90 }
                 { :timeout/seconds 120 }])

(def default-timeout (db/eid-by-av :timeout/seconds 90))

(defn get-timeout [timeout]
  (db/entity-by-av :timeout/seconds timeout))

(defn timeout-exists? [timeout]
  (some? (get-timeout timeout)))
