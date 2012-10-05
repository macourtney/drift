(defproject drift "1.5.0"
  :description "Drift is a rails like migration framework for Clojure."
  :dependencies [[clojure-tools "1.2.0"]
                 [org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.logging "0.2.3"]]
  :profiles {:dev {:dependencies [[log4j/log4j "1.2.16"]]}}

  :aot [drift.Drift])