(ns drift.runner
  (:import [java.io File])
  (:require [clojure.tools.logging :as logging]
            [drift.core :as core]
            [drift.listener-protocol :as listener-protocol]
            [drift.version :as version]))

(def listeners (atom []))

(defn add-listener [listener]
  (swap! listeners conj listener))

(defn remove-listener [listener]
  (swap! listeners (fn [listeners listener] (remove #(= listener %) listeners)) listener))
  
(defn runner-listeners []
  @listeners)

(defn start-migration [namespaces ^Boolean up?]
  (doseq [listener (runner-listeners)]
    (listener-protocol/start listener (map core/namespace-name-str namespaces) up?)))
  
(defn running-migration [^String namespace ^Boolean up?]
  (doseq [listener (runner-listeners)]
    (listener-protocol/running listener namespace up?)))
    
(defn end-migration []
  (doseq [listener (runner-listeners)]
    (listener-protocol/end listener)))

(defn
#^{ :doc "Runs the up function in the given migration file." }
  run-migrate-up [migration-namespace]
  (if migration-namespace
    (let [namespace-name (core/namespace-name-str migration-namespace)
          namespace-symbol (symbol namespace-name)]
      (logging/info (str "Running " namespace-name " up..."))
      (running-migration namespace-name (Boolean. true))
      (require namespace-symbol)
      (when-let [up-fn (ns-resolve namespace-symbol 'up)]
        (up-fn)
        (version/update-db-version (core/migration-number-from-namespace migration-namespace))))
    (logging/error (str "Invalid migration-namespace: " migration-namespace ". No changes were made to the database."))))
  
(defn
#^{ :doc "Runs the down function in the given migration file." }
  run-migrate-down [migration-namespace]
  (if migration-namespace
    (let [namespace-name (core/namespace-name-str migration-namespace)
          namespace-symbol (symbol namespace-name)]
      (logging/info (str "Running " namespace-name " down..."))
      (running-migration namespace-name (Boolean. false))
      (require namespace-symbol)
      (when-let [down-fn (ns-resolve namespace-symbol 'down)]
        (down-fn)
        (version/update-db-version
          (core/migration-number-before (core/migration-number-from-namespace migration-namespace)))))
    (logging/error (str "Invalid migration-file: " migration-namespace ". No changes were made to the database."))))

(defn
#^{ :doc "Runs the up function on all of the given migration files." }
  migrate-up-all
  ([] (migrate-up-all (core/migration-namespaces)))
  ([migration-namespaces]
    (start-migration (seq migration-namespaces) (Boolean. true))
    (let [output (when (and migration-namespaces (not-empty migration-namespaces))
                  (reduce max 0 (map run-migrate-up migration-namespaces)))]
      (end-migration)
      output)))

(defn
#^{ :doc "Runs the down function on all of the given migration files." }
  migrate-down-all
  ([] (migrate-down-all (reverse (core/migration-namespaces))))
  ([migration-namespaces]
    (start-migration (seq migration-namespaces) (Boolean. false))
    (let [output (when (and migration-namespaces (not-empty migration-namespaces))
                   (reduce min Long/MAX_VALUE (map run-migrate-down migration-namespaces)))]
      (end-migration)
      output)))

(defn
#^{ :doc "Migrates the database up from from-version to to-version." }
  migrate-up [from-version to-version] 
  (if (and from-version to-version)
    (if-let [new-version (migrate-up-all (core/migration-namespaces-in-range from-version to-version))]
      (logging/info (str "Migrated to version: " new-version))
      (logging/info "No changes were made to the database."))
    (logging/error (str "Invalid version number: " from-version " or " to-version ". No changes were made to the database."))))
  
(defn
#^{ :doc "Migrates the database down from from-version to to-version." }
  migrate-down [from-version to-version] 
  (if (and from-version to-version)
    (if-let [new-version (migrate-down-all (reverse (core/migration-namespaces-in-range to-version from-version)))]
      (logging/info (str "Migrated to version: " new-version))
      (logging/info "No changes were made to the database."))
    (logging/error (str "Invalid version number: " from-version " or " to-version ". No changes were made to the database."))))

(defn 
#^{ :doc "Updates the database to the given version number. If the version number is less than the current database 
version number, then this function causes a roll back." }
  update-to-version [version-number]
  (if version-number
    (let [db-version (version/current-db-version)]
      (logging/info (str "Current database version: " db-version))
      (let [version-number-min (min (max version-number 0) (core/max-migration-number))]
        (logging/info (str "Updating to version: " version-number-min))
        (if (< db-version version-number-min)
          (migrate-up (inc db-version) version-number-min)
          (migrate-down db-version (inc version-number-min)))))
    (logging/error (str "Invalid version-number: " version-number))))