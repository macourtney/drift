(ns drift.builder
  (:import [java.io File]
           [java.text SimpleDateFormat]
           [java.util Date])
  (:require [clojure.tools.logging :as logging]
            [clojure.tools.loading-utils :as loading-utils]
            [clojure.tools.string-utils :as util-string-utils]
            [drift.core :as core]))

(defn 
#^{ :doc "Finds or creates if missing, the migrate directory in the given db directory." }
  find-or-create-migrate-directory
  ([] (find-or-create-migrate-directory (core/migrate-directory))) 
  ([migrate-directory]
    (when migrate-directory
      (if (.exists migrate-directory)
        (logging/info "Migrate directory already exists.")
        (do
          (logging/info "Creating migrate directory...")
          (.mkdirs migrate-directory)))
      migrate-directory)))

(defn incremental-migration-number-generator []
  (util-string-utils/prefill (str (core/find-next-migrate-number)) 3 "0"))

(defn timestamp-migration-number-generator []
  (.format (SimpleDateFormat. "yyyyMMddHHmmss") (new Date)))

(defn migration-number-generator-fn []
  (or (:migration-number-generator (core/find-config)) timestamp-migration-number-generator))

(defn migration-number []
  ((migration-number-generator-fn)))

(defn
#^{ :doc "Creates a new migration file from the given migration name." }
  create-migration-file
  ([migration-name] (create-migration-file (find-or-create-migrate-directory) migration-name)) 
  ([migrate-directory migration-name]
    (if (and migrate-directory migration-name)
      (let [migration-file-name (str (migration-number) "_" (loading-utils/dashes-to-underscores migration-name) ".clj")
            migration-file (new File migrate-directory  migration-file-name)]
        (logging/info (str "Creating migration file " migration-file-name "..."))
        (.createNewFile migration-file)
        migration-file))))