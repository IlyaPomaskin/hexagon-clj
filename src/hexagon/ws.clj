(ns hexagon.ws
  (:use compojure.core
        org.httpkit.server)
  (:require [cheshire.core :as json]
            [clojure.string :as string]
            [hexagon.log :as log]
            [hexagon.game :as game]))

(defn handle-connect [username channel]
  (log/ws-info username "connected")
  (game/create-user username channel))

(defn handle-close [username status]
  (log/ws-info "close" status)
  (game/remove-user username))

(defn handle-receive [username msg]
  (try
    (let [decoded-msg (json/parse-string msg true)]
      (when-not (map? decoded-msg)
        (throw (Exception. "Wrong msg format")))
        (game/dispatch-message (assoc decoded-msg :username username)))
    (catch com.fasterxml.jackson.core.JsonParseException e
      (log/ws-error username "Can't parse json: " msg))))

(defn ws-handler [request]
  (let [username (-> request :params :username)
        has-username (not (string/blank? username))
        is-websocket (:websocket? request)]
    (when-not has-username
      (log/ws-error "username is blank"))
    (when-not is-websocket
      (log/ws-error username "not ws"))
    (when (and has-username is-websocket)
      (with-channel request channel
        (handle-connect username channel)
        (on-receive channel #(handle-receive username %1))
        (on-close channel #(handle-close username %1))))))
