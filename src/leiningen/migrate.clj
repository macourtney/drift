(ns leiningen.migrate
  "Run drift migration scripts."
  (use [leiningen.core.eval :only (eval-in-project)]))

(defn migrate [project & args]
  "Run migration scripts."
  (eval-in-project project
    `(drift.execute/run '~args)
    '(require 'drift.execute)))
