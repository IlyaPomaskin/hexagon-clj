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
    :game-settings/src-first-move? {}
    :invite/from { :db/valueType :db.type/ref
                   :db/unique :db.unique/identity }
    :invite/to { :db/valueType :db.type/ref
                 :db/unique :db.unique/identity }
    :invite/settings { :db/valueType :db.type/ref }
    :game/blue { :db/valueType :db.type/ref }
    :game/red { :db/valueType :db.type/ref }
    :game/settings { :db/valueType :db.type/ref }
    :game/board {}})

(def db (d/create-conn schema))

;; utils

(defn eid-by-av [a v]
  (d/q '{:find  [?e .]
         :in    [$ ?a ?v]
         :where [[?e ?a ?v]]} @db a v))

(defn entity-by-eid [eid]
  (d/entity @db eid))

(defn entity-by-av [a v]
  (when-let [eid (eid-by-av a v)]
    (entity-by-eid eid)))

(defn retract-by-av [a v]
  (d/transact! db [[ :db.fn/retractEntity (eid-by-av db a v) ]]))
