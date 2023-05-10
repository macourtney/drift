(ns drift.generator
  (:require [clojure.string :as string]
            [clojure.tools.logging :as logging]
            [drift.args :as args]
            [drift.builder :as builder]
            [drift.config :as config]
            [drift.core :as core]))

(defn
#^{ :doc "Prints out how to use the generate migration command." }
  migration-usage []
  (println "You must supply a migration name (Like migration-name).")
  (println "Usage: lein.bat migration <migration name>"))

(defn
  create-file-content [migration-namespace ns-content up-content down-content]
  (let [migration-number (core/migration-number-from-namespace migration-namespace)]
    (str "(ns " migration-namespace (or ns-content (config/default-ns-content)) ")

(defn up
  \"Migrates the database up to version " migration-number ".\"
  []
  " (or up-content (str "(println \"" migration-namespace " up...\")"))")

(defn down
  \"Migrates the database down from version " migration-number ".\"
  []
  " (or down-content (str "(println \"" migration-namespace " down...\")"))")")))

(defn
#^{ :doc "Generates the migration content and saves it into the given migration file." }
  generate-file-content [migration-file migration-name ns-content up-content down-content]
  (let [migration-namespace (core/migration-namespace migration-file)
        content (create-file-content migration-namespace ns-content up-content down-content)]
    (spit migration-file content)))

(defn
#^{ :doc "Creates the migration file from the given migration-name." }
  generate-migration-file 
    ([migration-name] (generate-migration-file migration-name (config/default-ns-content) nil nil))
    ([migration-name ns-content up-content down-content]
      (core/run-init [])
      (if migration-name
        (let [migrate-directory (builder/find-or-create-migrate-directory)
              migration-file (builder/create-migration-file migrate-directory migration-name)] 
          (generate-file-content migration-file migration-name ns-content up-content down-content))
        (migration-usage))
      (core/run-finished)))

(defn generate-migration-file-cmdline
  "parse command-line args from lein, set up any custom config,
   and invoke generate-migration-file"
  [config-fn-symbol args]
  (let [[opts [migration-name & remaining]] (args/parse-create-migration-args args)]
    (if (empty? remaining)
      (config/with-config-fn-symbol
        (or (:config opts) config-fn-symbol)
        (fn []
          (generate-migration-file migration-name)))
      (do (logging/error "Invalid arguments:" (string/join " " remaining))
          (args/print-usage "create-migration" args/create-migration-arg-specs "migration-name")))))
