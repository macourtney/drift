(ns drift.builder
  (:import [java.io File])
  (:require [clojure.contrib.logging :as logging]
            [clojure.contrib.str-utils :as str-utils]
            [drift.core :as core]))

(defn
#^{:doc "If the string's length does not equal total-length then this method returns a new string with length 
total-length by adding fill-char multiple times to the beginning of string. If string's length is already total-length,
then this method simply returns it."}
  prefill [string total-length fill-char]
  (let [base-string (if string string "")
        final-length (if total-length total-length 0)]
    (if (>= (.length base-string) final-length)
      base-string
      (str 
        (str-utils/str-join "" 
          (map
            (fn [index] fill-char) 
            (range (- final-length (.length base-string)))))
        base-string))))

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
      (let [next-migrate-number (core/find-next-migrate-number migrate-directory)
            migration-file-name (str (prefill (str next-migrate-number) 3 "0") "_" (core/dashes-to-underscores migration-name) ".clj")
            migration-file (new File migrate-directory  migration-file-name)]
        (logging/info (str "Creating migration file " migration-file-name "..."))
        (.createNewFile migration-file)
        migration-file))))