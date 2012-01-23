(defproject drift "1.4.4-SNAPSHOT"
  :description "Drift is a rails like migration framework for Clojure."
  :dependencies [[clojure-tools "1.1.1"]
                 [org.clojure/clojure "1.2.1"]
                 [org.clojure/tools.logging "0.2.3"]]
  :dev-dependencies [[log4j/log4j "1.2.16"]]

  :aot [drift.Drift])