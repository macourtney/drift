(defproject drift "1.3.0"
  :description "Drift is a rails like migration framework for Clojure."
  :dependencies [[clojure-tools "1.0.2"]
                 [org.clojure/clojure "1.2.1"]
                 [org.clojure/tools.logging "0.2.0"]]
  :dev-dependencies [[log4j/log4j "1.2.16"]]
  
  :aot [drift.Drift])