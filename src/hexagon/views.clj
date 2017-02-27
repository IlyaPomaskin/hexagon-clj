(ns hexagon.views
  (:require
    [hiccup
     [page :refer [html5]]
     [page :refer [include-js]]]))

(defn index-page []
  (html5
    [:head
     [:title "Hexagon"]
     [:script {:type "text/javascript"
               :id "lt_ws"
               :src "http://localhost:5678/socket.io/lighttable/ws.js"}]
     [:link {:rel "stylesheet"
             :href "https://unpkg.com/blaze"
             :crossorigin "anonymous"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]]
    [:body.c-text
     [:h1 "Hexagon"]
     [:div#container]
     (include-js "/js/main.js")]))
