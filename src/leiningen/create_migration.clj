(ns leiningen.create-migration
  (:require [leiningen.compile :as lein-compile]))

(defn create-migration [project & args]
  (lein-compile/eval-in-project project
    `(do
      (require ~''drift.builder)
      (drift.builder/create-migration-file ~(first args)))))

