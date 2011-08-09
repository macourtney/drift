(ns leiningen.migrate
  (require [leiningen.compile :as lein-compile]))

(defn migrate [project & args]
  (lein-compile/eval-in-project project
    `(drift.execute/run '~args)
    nil nil '(require 'drift.execute)))