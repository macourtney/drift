(ns sample.migrate-config
  (:use drift.core)
  (:require [drift.builder :as builder]
            [drift-db.migrate :as drift-db-migrate]))

;; This is a sample migrate-config file. All possible options are listed with comments.

(defn init-flavor [args]
  (println "Initializing drift."))

(defn finished []
  (println "Action has been run."))

(defn migrate-namespaces [migrate-dir-name migrate-namespace-prefix]
  (migration-namespaces))

(defn migrate-config []
   {

     ; This is a function takes no arguments and returns the database's current version. In the example below, the
     ; drift-db current-version function is used. Required.
     :current-version drift-db-migrate/current-version

     ; This is a function which takes the version from Drift and set the database to that version. In the example below,
     ; the drift-db update-version function is used. Required.
     :update-version drift-db-migrate/update-version

     ; This is the path where your migrations can be found. It is :src path, in this
     ; case '/src', is not included in the namespace for the migrations. Defaults to "/src/migrations" Optional.
     :directory "/src/database/migrations"

     ; This is the source directory path. This path is subtracted from the :directory to determine how to generate the
     ; namespace-prefix. Defaults to "/src"
     :src "/src"

     ; This is the namespace prefix for each migration. The namespace prefix is the part of the migration namespace not
     ; including the file name. For example, for the migration file 001_test.clj, the full namespace calculated from the
     ; namespace-prefix would be database.migrations.001-test. If the namespace prefix is not set, then it is calculated
     ; from the :directory. Optional.
     :namespace-prefix "database.migrations"

     ; This is the initialization function which will be called before any migrations are run. If you have any
     ; initialization code which needs to be called before your migrations are called, add it to this function. The init
     ; function must accept a single argument which will be a list of arguments passed to drift excluding the version
     ; argument. Init may be called multiple times before the migrations are run. Optional.
     :init init-flavor

     ; This is a function which will be called after the action has been run. Optional.
     :finished finished

     ; This is a function which returns the next number to use when creating a new migration file. There are two
     ; migration number generators included with Drift: incremental-migration-number-generator and
     ; timestamp-migration-number-generator. The incremental-migration-number-generator simply starts with '001' and
     ; increments the migration number for each new migration file. The timestamp-migration-number-generator uses the
     ; current date and time to create the migration number. If the migration number generator is not given, the
     ; timestamp-migration-number-generator is used.
     :migration-number-generator builder/incremental-migration-number-generator

     ; This is extra namespace content added when Drift generates a new migration file. The text is simply added in the
     ; call to ns, after the namespace. Optional.
     :ns-content "\n  (:use drift-db.core)"

     ; This is a function which takes the migration directory and the namespace prefix and returns all of the migration
     ; namespaces. If this option is not set, Drift uses the :directory and :namespace-prefix to find all of the
     ; migration namespaces. You should only have to use this option if your directory structure is very unusual.
     ; Optional.
     :migration-namespaces migrate-namespaces
     })