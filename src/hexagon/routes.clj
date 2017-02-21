(ns hexagon.routes
  (:use compojure.core
        hexagon.views
        [hiccup.middleware :only (wrap-base-url)]
        [org.httpkit.server :only (run-server)])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [hexagon.ws :as ws]))

(defroutes main-routes
  (GET "/" [] (index-page))
  (GET "/ws" request (ws/ws-handler request))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> (handler/site main-routes)
      (wrap-base-url)))

(defonce serv (run-server app {:port 8080}))

(defn -main [] (println "Started"))
