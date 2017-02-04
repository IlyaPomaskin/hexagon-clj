(defproject hexagon "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]
;;                  [org.clojure/clojurescript "1.9.293"]
                 [javax.servlet/servlet-api "2.5"]
                 [http-kit "2.2.0"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.4"]
                 [cheshire "5.7.0"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [lein-light-nrepl "0.3.3"]
                 [org.omcljs/om "0.9.0"]]
;;   :plugins [[lein-cljsbuild "1.1.5"]
;;             [lein-ring "0.8.7"]]
;;   :cljsbuild {
;;     :builds [{
;;         :source-paths ["src-cljs"]
;;         :compiler {
;;           :output-to "resources/public/js/main.js"
;;           :optimizations :whitespace
;;           :pretty-print true}}]}
  :main hexagon.routes
;;   :aot [hexagon.routes]
  :repl-options { :port 35975
                  :repl-verbose true
                  :nrepl-middleware [lighttable.nrepl.handler/lighttable-ops]})
