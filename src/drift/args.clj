(ns drift.args
  (:require [clojure.string :as string]
            [drift.config :as config]))

(defn split-args
  "split an arglist using a matcher fn : returns
   [args-before-match, args-including-and-after-match]"
  [args matcher]
  (split-with #(not-any? (partial = %) matcher) args))

(defn remove-opt
  "given a matched option and whatever is after it in the arg list, remove the option
   and return [{opt-key opt-value} remaining-args]"
  [[switch val & rest :as args] spec]
  (let [parser (or (:parser spec) identity)]
    (if (and switch val)
      [{(:key spec) (parser val)} rest]
      [{} args])))

(defn parse-args
  "do a partial parse of args... removing only options we know about and leaving everything else to
   be passed on to the user-supplied init function. tools.cli is no use for this"
  [args specs]
  (reduce (fn [[opts args] spec]
                 (let [[before match-rest] (split-args args (:matcher spec))
                       [new-opts after] (remove-opt match-rest spec)]

                   [(merge opts new-opts) (vec (concat before after))]))
          [{} args]
          specs))

(def config-arg-spec
  {:key :config
   :matcher ["-c" "-config" "--config"]
   :parser symbol
   :default config/default-config-fn-symbol
   :desc "Fully qualified name of function returning a Drift configuration map."})

(def migrate-arg-specs
  [{:key :version
    :matcher ["-v" "-version" "--version"]
    :desc "The version number to migrate to. Can be lower than the current version to roll back migrations."}
   config-arg-spec])

(defn parse-migrate-args
  [args]
  (parse-args args migrate-arg-specs))

(def create-migration-arg-specs
  [config-arg-spec])

(defn parse-create-migration-args
  [args]
  (parse-args args create-migration-arg-specs))

(defn print-usage
  #^{:doc "Prints out how to use a command with a given name and specification."}
  [cmd spec & [required-arg]]
  (println "Usage: lein" cmd "[options]" (or required-arg ""))
  (println "Options:")
  (doseq [arg spec]
    (println (str " " (string/join " " (:matcher arg)) "\t" (:desc arg)))
    (when (:default arg) (println "\t\t\tDefault value:" (:default arg)))))
