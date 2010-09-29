(ns drift.builder
  (:import [java.io File])
  (:require [clojure.contrib.logging :as logging]
            [clojure.contrib.str-utils :as str-utils]
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

(defn
#^{ :doc "Creates a new migration file from the given migration name." }
  create-migration-file
  ([migration-name] (create-migration-file (find-or-create-migrate-directory) migration-name)) 
  ([migrate-directory migration-name]
    (if (and migrate-directory migration-name)
      (let [next-migrate-number (core/find-next-migrate-number)
            migration-file-name (str (util-string-utils/prefill (str next-migrate-number) 3 "0") "_" (loading-utils/dashes-to-underscores migration-name) ".clj")
            migration-file (new File migrate-directory  migration-file-name)]
        (logging/info (str "Creating migration file " migration-file-name "..."))
        (.createNewFile migration-file)
        migration-file))))