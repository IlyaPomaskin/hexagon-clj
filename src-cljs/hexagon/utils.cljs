(ns hexagon.utils
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]))

(def current-username
  (-> js/window
      .-__INITIAL_STATE__
      .-username))

(defn get-current-user-eid []
  (d/q '[:find ?e .
         :in $ ?username
         :where [?e :user/name ?username]] @db current-username))
