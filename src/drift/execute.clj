(ns drift.execute
  (:require [drift.args :as args]
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

(defn
  migrate [version remaining-args]
  (core/with-init-config remaining-args
    (fn []
      (runner/update-to-version (version-number version)))))

(defn
  run [args]
  (let [[opts remaining] (args/parse-migrate-args args)]
    (config/with-config-fn-symbol (:config opts)
      (fn []
        (migrate (:version opts) remaining)))))
