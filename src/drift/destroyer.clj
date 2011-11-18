(ns drift.destroyer
  (:import [java.io File])
  (:require [clojure.tools.logging :as logging]
            [drift.core :as core]))

(defn
#^{:doc "Prints out how to use the destroy migration command."}
  migration-usage []
  (println "You must supply a migration name (Like migration-name).")
  (println "Usage: ./run.sh script/destroy.clj migration <migration name>"))

(defn
#^{:doc "Creates the migration file from the given migration-name."}
  destroy-migration-file [migration-name]
  (if migration-name
    (if-let [migrate-directory (core/find-migrate-directory)]
      (if-let [migration-file (core/find-migration-file migrate-directory migration-name)]
         (let [is-deleted (.delete migration-file)]
           (logging/info (str "File " (.getPath migration-file) (if is-deleted " deleted." " not deleted.") )))
         (logging/error (str "Could not find migration file for " migration-name)))
      (logging/error "Could not find db directory."))
    (migration-usage)))