(defproject game-of-life "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [org.clojure/core.async "0.2.374"]]

  :node-dependencies [[source-map-support "0.2.8"]]

  :plugins [[lein-cljsbuild "1.0.4"]
            [lein-npm "0.4.0"]]

  :source-paths ["src" "target/classes"]

  :clean-targets ["out" "out-adv"]

  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src"]
              :compiler {
                :main game-of-life.core
                :output-to "out-dev/game_of_life.js"
                :output-dir "out-dev"
                :optimizations :none
                :cache-analysis true
                :source-map true}}
             {:id "release"
              :source-paths ["src"]
              :compiler {
                :main game-of-life.core
                :output-to "out/game_of_life.min.js"
                :output-dir "out"
                :optimizations :advanced
                :pretty-print false}}]})
