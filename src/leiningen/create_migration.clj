(ns leiningen.create-migration
  "Create a new versioned migration script."
  (use [leiningen.core.eval :only (eval-in-project)]))

(defn create-migration [project & args]
  "Create a new migration file."
  (eval-in-project
    (update-in project [:dependencies]
      conj ['drift "1.4.5"])
    `(drift.generator/generate-migration-file ~(first args))
    '(require 'drift.generator)))

