(ns drift.runner
  (:import [java.io File])
  (:require [clojure.contrib.logging :as logging]
            [drift.core :as core]
            [drift.version :as version]))

(defn
#^{ :doc "Runs the up function in the given migration file." }
  run-migrate-up 
  ([migration-file] (run-migrate-up migration-file false))
  ([migration-file quiet]
    (if (and migration-file (instance? File migration-file))
      (do
        (logging/info (str "Running " (. migration-file getName) " up..."))
        (load-file (. migration-file getAbsolutePath))
        (load-string (str "(" (core/migration-namespace migration-file) "/up)"))
        (let [new-version (core/migration-number-from-file migration-file)]
          (version/update-db-version new-version)
          new-version))
      (logging/error (str "Invalid migration-file: " migration-file ". No changes were made to the database.")))))
  
(defn
#^{ :doc "Runs the down function in the given migration file." }
  run-migrate-down 
  ([migration-file] (run-migrate-down migration-file false))
  ([migration-file quiet]
    (if (and migration-file (instance? File migration-file))
      (do
        (logging/info (str "Running " (. migration-file getName) " down..."))
        (load-file (. migration-file getAbsolutePath))
        (load-string (str "(" (core/migration-namespace migration-file) "/down)"))
        (let [new-version (core/migration-number-before (core/migration-number-from-file migration-file))]
          (version/update-db-version new-version)
          new-version))
      (logging/error (str "Invalid migration-file: " migration-file ". No changes were made to the database.")))))
        
(defn
#^{ :doc "Runs the up function on all of the given migration files." }
  migrate-up-all
  ([] (migrate-up-all (core/all-migration-files) false))
  ([migration-files] (migrate-up-all migration-files false))
  ([migration-files quiet]
    (loop [other-migrations migration-files
           output nil]
      (if (not-empty other-migrations)
        (recur
          (rest other-migrations)
          (run-migrate-up (first other-migrations) quiet))
        output))))

(defn
#^{ :doc "Runs the up function on all of the given migration files." }
  migrate-down-all
  ([] (migrate-down-all (reverse (core/all-migration-files)) false))
  ([migration-files] (migrate-down-all migration-files false))
  ([migration-files quiet]
    (loop [other-migrations migration-files
           output nil]
      (if (not-empty other-migrations)
        (recur
          (rest other-migrations)
          (run-migrate-down (first other-migrations) quiet))
        output))))

(defn
#^{ :doc "Migrates the database up from from-version to to-version." }
  migrate-up
  ([from-version to-version] (migrate-up from-version to-version false))
  ([from-version to-version quiet]
    (if (and from-version to-version)
      (let [new-version (migrate-up-all (core/migration-files-in-range from-version to-version) quiet)]
        (if new-version
          (logging/info (str "Migrated to version: " new-version))
          (logging/info "No changes were made to the database.")))
      (logging/error (str "Invalid version number: " from-version " or " to-version ". No changes were made to the database.")))))
  
(defn
#^{ :doc "Migrates the database down from from-version to to-version." }
  migrate-down
  ([from-version to-version] (migrate-down from-version to-version false))
  ([from-version to-version quiet]
    (if (and from-version to-version)
      (let [new-version (migrate-down-all (reverse (core/migration-files-in-range to-version from-version)) quiet)]
        (if new-version
          (logging/info (str "Migrated to version: " new-version))
          (logging/info "No changes were made to the database.")))
      (logging/error (str "Invalid version number: " from-version " or " to-version ". No changes were made to the database.")))))

(defn 
#^{ :doc "Updates the database to the given version number. If the version number is less than the current database 
version number, then this function causes a roll back." }
  update-to-version
  ([version-number] (update-to-version version-number false))
  ([version-number quiet]
    (if version-number
      (let [db-version (version/current-db-version)]
        (logging/info (str "Current database version: " db-version))
        (let [version-number-min (min (max version-number 0) (core/max-migration-number))]
          (logging/info (str "Updating to version: " version-number-min))
          (if (< db-version version-number-min)
            (migrate-up (+ db-version 1) version-number-min quiet)
            (migrate-down db-version (+ version-number-min 1) quiet))))
      (logging/error (str "Invalid version-number: " version-number)))))