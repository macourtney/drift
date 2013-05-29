(ns drift.Drift
  (:require [drift.core :as core]
            [drift.execute :as execute]
            [drift.runner :as runner]
            [drift.version :as version])
  (:import [drift.listener_protocol ListenerProtocol])
  (:gen-class
    :methods [[init [java.util.List] Object]
              [migrate [Long java.util.List] Void]
              [migrationCount [Long java.util.List] Integer]
              [currentVersion [] Long]
              [maxMigrationNumber [] Long]
              [addListener [drift.listener_protocol.ListenerProtocol] java.util.Collection]
              [removeListener [drift.listener_protocol.ListenerProtocol] java.util.Collection]]))

(defn -init [_ other-args]
  (core/run-init other-args))

(defn -migrate [_ version other-args]
  (execute/migrate version other-args))

(defn -migrationCount [_ version other-args]
  (execute/migration-count version other-args))

(defn -currentVersion [_]
  (long (version/current-db-version)))

(defn -maxMigrationNumber [_]
  (long (core/max-migration-number)))

(defn -addListener [_ listener]
  (runner/add-listener listener))

(defn -removeListener [_ listener]
  (runner/remove-listener listener))