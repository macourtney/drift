(ns leiningen.create-migration
  (:require [leiningen.compile :as lein-compile]))

(defn create-migration [project & args]
  (lein-compile/eval-in-project project
    `(do
      (require ~''drift.generator)
      (drift.generator/generate-migration-file ~(first args)))))

