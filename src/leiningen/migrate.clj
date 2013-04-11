(ns leiningen.migrate
  "Run drift migration scripts."
  (use [leiningen.core.eval :only (eval-in-project)]))

(defn migrate [project & args]
  "Run migration scripts."
  (eval-in-project
    (update-in project [:dependencies]
      conj ['drift "1.4.5"])
    `(drift.execute/run '~args)
    '(require 'drift.execute)))
