(defproject hexagon "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [datascript "0.18.13"]
                 [org.clojure/clojurescript "1.9.293"]
                 [lein-figwheel "0.5.9"]
                 [javax.servlet/servlet-api "2.5"]
                 [http-kit "2.2.0"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.4"]
                 [cheshire "5.7.0"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [lein-light-nrepl "0.3.3"]
                 [rum "0.10.8"]
                 [javax.xml.bind/jaxb-api "2.3.1"]]
  :plugins [[lein-cljsbuild "1.1.5"]
            [lein-ring "0.8.7"]
            [lein-figwheel "0.5.9"]]
  :source-paths ["src"]
  :main hexagon.routes
  :cljsbuild { :builds [{ :id "main"
                          :source-paths ["src-cljs"]
                          :figwheel true
                          :compiler { :main "hexagon.main"
                                      :asset-path "js/out"
                                      :output-to "resources/public/js/main.js"
                                      :output-dir "resources/public/js/out"
                                      :optimizations :none
                                      :verbose true
                                      :pretty-print true
                                      :source-map true }}]})
