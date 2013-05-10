(ns drift.args)

(defn split-args
  "split an arglist using a matcher fn : returns
   [args-before-match, args-including-and-after-match]"
  [args matcher]
  (split-with #(not (matcher %)) args))

(defn remove-opt
  "given a matched option and whatever is after it in the arg list, remove the option
   and return [{opt-key opt-value} remaining-args]"
  [[switch val & rest :as args] spec]
  (if (and switch val)
    [{(:key spec) val} rest]
    [{} args]))


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

(def migrate-arg-specs
  [{:key :version
    :matcher #{"-v" "-version" "--version"}}
   {:key :config
    :matcher #{"-c" "-config" "--config"}}])

(defn parse-migrate-args
  [args]
  (parse-args args migrate-arg-specs))

(def create-migration-arg-specs
  [{:key :config
    :matcher #{"-c" "-config" "--config"}}])

(defn parse-create-migration-args
  [args]
  (parse-args args create-migration-arg-specs))
