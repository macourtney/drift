(ns leiningen.create-migration
  "Create a new versioned migration script."
  (:require [drift.drift-version :as drift-version])
  (:use [leiningen.core.eval :only (eval-in-project)]))

(defn create-migration [project & args]
  "Create a new migration file."
  (eval-in-project
    (update-in project [:dependencies]
      conj ['drift drift-version/version])
    `(drift.generator/generate-migration-file-cmdline '~args)
    '(require 'drift.generator)))
