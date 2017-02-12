(ns hexagon.db
  (:require [datascript.core :as d]))

(def schema
  { :user/name { :db/cardinality :db.cardinality/one
                 :db/unique :db.unique/value }
    :user/channel {}
    :user/playing? {}
    :user/invites { :db/valueType :db.type/ref
                    :db/cardinality :db.cardinality/many }
    :board/name {}
    :board/map {}
    :game-settings/board { :db/valueType :db.type/ref }
    :game-settings/timeout {}
    :game-settings/src-firstmove? {}
    :invite/from { :db/valueType :db.type/ref }
    :invite/to { :db/valueType :db.type/ref }
    :invite/settings { :db/valueType :db.type/ref }})

(def db (d/create-conn schema))
