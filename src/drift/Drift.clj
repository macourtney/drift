(ns drift.Drift
  (:require [drift.core :as core]
            [drift.execute :as execute]
            [drift.version :as version])
  (:gen-class
    :methods [[init [java.util.List] Object]
              [migrate [Integer java.util.List] Void]
              [currentVersion [] Integer]
              [maxMigrationNumber [] Integer]]))

(defn -init [_ other-args]
  (core/run-init other-args))

(defn -migrate [_ version other-args]
  (execute/migrate version other-args))

(defn -currentVersion [_]
  (version/current-db-version))

(defn -maxMigrationNumber [_]
  (core/max-migration-number))