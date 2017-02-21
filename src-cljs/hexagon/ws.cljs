(ns hexagon.ws)

(defonce chan (atom nil))

(defn make! [username handler-fn]
  (if-let [ws (js/WebSocket. (str "ws://" (.-host js/location) "/ws?username=" username))]
    (do
      (set! (.-onopen ws) #(println "Open" username))
      (set! (.-onmessage ws) #(->> %1 .-data (. js/JSON parse) handler-fn))
      (set! (.-onclose ws) #(println "Close" username))
      (reset! chan ws))
    (throw (js/Error. "Websocket connection failed!"))))
