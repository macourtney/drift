(ns drift.execute
  (:require [clojure.string :as string]
            [clojure.tools.logging :as logging]
            [drift.args :as args]
            [drift.config :as config]
            [drift.core :as core]
            [drift.runner :as runner]))

(defn
#^{:doc "Gets the version number from the passed in version parameter. If the given version string is nil, then this method returns Long/MAX_VALUE. If the version parameter is invalid, then this method prints an error and returns nil."}
  version-number [version]
  (if version
    (if (string? version)
      (Long/parseLong version)
      version)
    Long/MAX_VALUE))

(defn migration-count
  "Returns the total number of migrations to run to update the database to the given version number."
  [version remaining-args]
  (core/with-init-config remaining-args
    (fn []
      (runner/migration-count (version-number version)))))

(defn
  migrate [version remaining-args]
  (core/with-init-config remaining-args
    (fn []
      (runner/update-to-version (version-number version)))))

(defn
  run [config-fn-symbol args]
  (let [[opts remaining] (args/parse-migrate-args args)]
    (if (empty? remaining)
      (config/with-config-fn-symbol
        (or (:config opts) config-fn-symbol)
        (fn []
          (migrate (:version opts) remaining)))
      (do (logging/error "Invalid arguments:" (string/join " " remaining))
          (args/print-usage "migrate" args/migrate-arg-specs)))))
