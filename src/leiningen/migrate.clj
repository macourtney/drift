(ns leiningen.migrate
  (require [leiningen.compile :as lein-compile]))

(defn migrate [project & args]
  (lein-compile/eval-in-project project
    `(do
      (use ~''drift.execute)
      (drift.execute/run '~args))))