(ns hexagon.entities.user
  (:require [datascript.core :as d]
            [hexagon.db :as db :refer [db]]
            [hexagon.entities.board :as board]
            [hexagon.entities.timeout :as timeout]))

(defn get [username]
  (db/entity-by-av :user/name username))

(defn get-eid [username]
  (db/eid-by-av :user/name username))

(defn get-by-eid [eid]
  (db/entity-by-eid eid))

(def filters (atom {}))

(defn add [username channel]
  (d/transact! db [{ :user/name username
                     :user/channel channel
                     :user/playing? false }])
  (let [eid (db/eid-by-av :user/name username)]
    (swap! filters assoc eid (create-user-datoms-filter eid))))

(defn delete [username]
  (let [eid (db/eid-by-av :user/name username)]
    (swap! filters dissoc eid)
    (d/transact! db [[ :db.fn/retractEntity eid ]])))

(defn exists? [username]
  (some? (db/eid-by-av :user/name username)))

(defn get-usernames []
  (d/q '[:find [?name ...]
         :where [_ :user/name ?name]] @db))

(defn playing? [username]
  (:playing? (db/entity-by-av :user/name username)))

(defn create-user-datoms-filter [user-eid]
  (fn [{:keys [e a v]}]
    (let [ns-prefix (namespace a)
          user-own-game? #(or (= (:game/red %1) user-eid)
                              (= (:game/blue %1) user-eid))
          find-invite-by #(d/q '[:find ?e .
                                 :in $ ?attr ?user-eid ?settings-eid
                                 :where
                                 [?e ?attr ?user-eid]
                                 [?e :invite/settings ?settings-eid]] db %1 %2 %3)]
      (or
        (= ns-prefix "board")
        (= ns-prefix "timeout")
        (and (= ns-prefix "user")
             (not= (name a) "channel"))
        (and (= ns-prefix "invite")
             (let [invite (d/entity db e)]
               (or (= (:invite/from invite)
                      user-eid)
                   (= (:invite/to invite)
                      user-eid))))
        (and (= ns-prefix "game-settings")
             (or (some? (find-invite-by :invite/from user-eid e))
                 (some? (find-invite-by :invite/to user-eid e))))
        (and (= ns-prefix "game")
             (let [game (db/entity-by-eid e)]
               (user-own-game? game)))
        (and (= ns-prefix "cell")
             (let [cell (db/entity-by-eid e)]
               (when-some [game (db/entity-by-eid (:cell/game cell))]
                          (user-own-game? game))))))))
