(ns hexagon.db
  (:require [datascript.core :as d]))

(def schema
  { :user/name { :db/cardinality :db.cardinality/one
                 :db/unique :db.unique/identity  }
    :user/channel {}
    :user/playing? {}
    :user/invites { :db/valueType :db.type/ref
                    :db/cardinality :db.cardinality/many }
    :board/name { :db/cardinality :db.cardinality/one
                  :db/unique :db.unique/identity }
    :board/map {}
    :timeout/seconds {}
    :game-settings/board { :db/valueType :db.type/ref }
    :game-settings/timeout { :db/valueType :db.type/ref }
    :game-settings/src-firstmove? {}
    :invite/from { :db/valueType :db.type/ref }
    :invite/to { :db/valueType :db.type/ref }
    :invite/settings { :db/valueType :db.type/ref }})

(def db (d/create-conn schema))

;; utils

(defn eid-by-av [a v]
  (-> (d/datoms @db :avet a v)
      first
      :e))

(defn entity-by-eid [eid]
  (d/entity @db eid))

(defn entity-by-av [a v]
  (when-let [eid (eid-by-av db a v)]
    (entity-by-eid db eid)))

(defn retract-by-av [a v]
  (d/transact! db [[ :db.fn/retractEntity (eid-by-av db a v) ]]))
