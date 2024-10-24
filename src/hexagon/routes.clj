(ns hexagon.routes
  (:use compojure.core
        [hiccup.middleware :only (wrap-base-url)]
        [org.httpkit.server :only (run-server)])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.string :as string]
            [hexagon.entities.user :as user]
            [hexagon.views :as view]
            [hexagon.ws :as ws]))

(defn validate-form [form]
  (let [username (get form "username")]
    (cond
      (string/blank? username) {:username "Enter username"}
      (user/exists? username) {:username "Username already used"}
      :else nil)))

; FIXME restore username validation
(defn authorize [{params :query-params}]
  (if-let [errors (validate-form params)]
    ;(view/index-page errors)
    (view/game-page)))

(defroutes main-routes
           (GET "/" [] (view/index-page))
           (GET "/game" [] (view/game-page))
           (GET "/ws" request (ws/ws-handler request))
           (route/resources "/")
           (route/not-found "Page not found"))

(def app
  (-> (handler/site main-routes)
      (wrap-base-url)))

(defonce server (atom nil))

(defn start-server []
  (when (nil? @server)
    (reset! server (run-server app {:port 8080}))))

(defn stop-server []
  (when-not (nil? @server)
    (reset! server nil)))

(defn restart-server []
  (stop-server)
  (start-server))

(defn -main [] (println "Started"))
