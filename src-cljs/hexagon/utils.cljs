(ns hexagon.utils
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]))

(def current-username
  (let [search (-> js/document .-location .-search)
        search-params (js/URLSearchParams. search)]
    (.get search-params "username")))

(defn get-current-user-eid []
  (d/q '[:find ?e .
         :in $ ?username
         :where [?e :user/name ?username]] @db current-username))
