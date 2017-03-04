(ns hexagon.views
  (:require
    [cheshire.core :as json]
    [hiccup
     [page :refer [html5]]
     [page :refer [include-js]]]))

(def head
  [:head
   [:title "Hexagon"]
   [:script { :type "text/javascript"
              :id "lt_ws"
              :src "http://localhost:5678/socket.io/lighttable/ws.js" }]
   [:link { :rel "stylesheet"
            :href "https://unpkg.com/blaze"
            :crossorigin "anonymous" }]
   [:meta { :name "viewport"
            :content "width=device-width, initial-scale=1" }]])

(defn body [& additional]
  [:body.c-text
   (concat
     [[:h1.c-heading.u-centered "Hexagon"]]
     additional)])

(defn index-page
  ([]
   (index-page {}))
  ([{ username-error :username }]
   (html5
     head
     (body
       [:div.o-container.o-container--xsmall
        [:form { :method "post"
                 :action "/" }
         [:fieldset.o-fieldset
          [:h3.c-heading.u-centered "Enter your username"]
          [:div.o-form-element
           [:input { :type "text"
                     :name "username"
                     :class (str "c-field"
                                 (when username-error " c-field--error"))}]
           (when username-error
             [:div.c-hint.c-hint--static.c-hint--error username-error])]
          [:div.o-form-element.u-centered
           [:input.c-button { :type "submit"
                              :value "Login" }]]]]]))))

(defn game-page [initial-state]
  (html5
    head
    (body
      [:div#container]
      [:script { :type "text/javascript" }
       (str "window.__INITIAL_STATE__ = " (str (json/encode initial-state)) ";")]
      (include-js "/js/main.js"))))
