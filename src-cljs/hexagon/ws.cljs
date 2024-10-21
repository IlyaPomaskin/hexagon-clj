(ns hexagon.ws)

(defonce socket (atom nil))

(defn send! [msg]
  (->> msg
       cljs.core/clj->js
       (. js/JSON stringify)
       (.send @socket)))

(defn make! [username handler-fn]
  (if-let [ws (js/WebSocket. (str "ws://" (.-host js/location) "/ws?username=" username))]
    (do
      (set! (.-onopen ws) #(println "Open" username))
      (set! (.-onmessage ws) #(->> %1 .-data (. js/JSON parse) handler-fn))
      (set! (.-onclose ws) #(println "Close" username))
      (reset! socket ws))
    (throw (js/Error. "Websocket connection failed!"))))

(defn close! []
  (when-some [ws @socket] (.close ws)))