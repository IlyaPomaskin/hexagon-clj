(ns hexagon.views
  (:require
    [hiccup
     [page :refer [html5]]
     [page :refer [include-js]]]))

(defn index-page []
  (html5
    [:head
     [:title "Hello World"]
     [:script {:type "text/javascript"
               :id "lt_ws"
               :src "http://localhost:5678/socket.io/lighttable/ws.js"}]]
    [:body
     [:h1 "Html header"]
     [:div#container]
     (include-js "/js/main.js")]))
