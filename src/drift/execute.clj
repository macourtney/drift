(ns drift.execute
  (:require [clojure.contrib.command-line :as command-line]
            [drift.core :as core]
            [drift.runner :as runner]))

(defn
#^{:doc "Gets the version number from the passed in version parameter. If the given version string is nil, then this method returns Integer.MAX_VALUE. If the version parameter is invalid, then this method prints an error and returns nil."}
  version-number [version]
  (if version
    (if (string? version)
      (Integer/parseInt version)
      version)
    Integer/MAX_VALUE))

(defn
  migrate [version remaining-args]
  (core/run-init remaining-args)
  (runner/update-to-version (version-number version)))

(defn
  find-version-arg 
  ([args] (find-version-arg args []))
  ([args others]
    (let [first-arg (first args)]
      (if (= first-arg "-version")
        [(second args) (concat (reverse others) (drop 2 args))]
        (if-let [more-args (seq (rest args))]
          (recur more-args (cons first-arg others))
          [nil (reverse others)])))))

(defn
  run [args]
  (let [[version remaining] (find-version-arg args)]
    (migrate version remaining)))