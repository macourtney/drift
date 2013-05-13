(ns leiningen.create-migration
  "Create a new versioned migration script."
  (use [leiningen.core.eval :only (eval-in-project)]))

(defn create-migration [project & args]
  "Create a new migration file."
  (eval-in-project project
    `(drift.generator/generate-migration-file-cmdline '~args)
    '(require 'drift.generator)))
