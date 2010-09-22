(ns drift.core
  (:import [java.io File])
  (:require [clojure.string :as string]
            [clojure.contrib.find-namespaces :as find-namespaces]
            [clojure.contrib.logging :as logging]
            [clojure.contrib.seq-utils :as seq-utils]
            [clojure.contrib.str-utils :as clojure-str-utils]))

(def migrate-dir "migrate")

(def config-ns-symbol 'config.migrate-config)

(defn
#^{:doc "Converts all dashes to underscores in string."}
  dashes-to-underscores [string]
  (if string
    (clojure-str-utils/re-gsub #"-" "_" string)
    string))

(defn
#^{:doc "Converts all underscores to dashes in string."}
  underscores-to-dashes [string]
  (if string
    (clojure-str-utils/re-gsub #"_" "-" string)
    string))

(defn
#^{:doc "Converts all slashes to periods in string."}
  slashes-to-dots [string]
  (if string
    (clojure-str-utils/re-gsub #"/|\\" ; "\" Fixing a bug with syntax highlighting
       "." string) 
    string))

(defn
#^{ :doc "Finds the config namespace." }
  find-config-namespace []
  (require config-ns-symbol)
  (find-ns config-ns-symbol))

(defn
#^{ :doc "Finds the config map." }
  find-config []
  (when-let [migrate-config-namespace (find-config-namespace)]
    (when-let [migrate-config-fn (ns-resolve migrate-config-namespace 'migrate-config)]
      (migrate-config-fn))))

(defn
#^{ :doc "Finds the init function from the config. The init function should be run before each migration." }
  find-init-fn []
  (:init (find-config)))

(defn
#^{ :doc "Runs the init function with the given args." }
  run-init [args]
  (when-let [init-fn (find-init-fn)]
    (init-fn args)))

(defn
  default-ns-content []
  (:ns-content (find-config)))

(defn
#^{ :doc "Finds the migrate directory name." }
  find-migrate-dir-name []
  (or (:directory (find-config)) migrate-dir))

(defn
#^{:doc "Returns the directory where Conjure is running from."}
  user-directory []
  (new File (.getProperty (System/getProperties) "user.dir")))

(defn
  migrate-directory []
  (File. (user-directory) (find-migrate-dir-name)))

(defn
#^{:doc "Returns the file object if the given file is in the given directory, nil otherwise."}
  find-directory [directory file-name]
  (when (and file-name directory (string?  file-name) (instance? File directory))
    (let [file (File. (.getPath directory) file-name)]
      (when (and file (.exists file))
        file))))

(defn
#^{ :doc "Finds the migrate directory." }
  find-migrate-directory []
  (let [user-directory (user-directory)
        migrate-dir-name (find-migrate-dir-name)]
    (find-directory user-directory migrate-dir-name)))

(defn
  file-separator-index [path-str]
  (when path-str
    (let [min-args (filter #(> % -1) [(.indexOf path-str "/" 1) (.indexOf path-str "\\" 1)])]
      (when (not-empty min-args)
        (apply min min-args)))))

(defn
#^{ :doc "Returns the namespace prefix for the migrate directory name." }
  migrate-namespace-prefix-from-directory
  ([] (migrate-namespace-prefix-from-directory (find-migrate-dir-name)))
  ([migrate-dir-name]
    (when migrate-dir-name
      (let [file-separator-index (file-separator-index migrate-dir-name)
            trimmed-dir-name (if file-separator-index
                               (.substring migrate-dir-name (inc file-separator-index))
                               migrate-dir-name)]
        (slashes-to-dots (underscores-to-dashes trimmed-dir-name))))))

(defn
  migrate-namespace-prefix []
  (or (:namespace-prefix (find-config)) (migrate-namespace-prefix-from-directory)))

(defn
  namespace-name-str [migration-namespace]
  (when migration-namespace
    (if (string? migration-namespace)
      migration-namespace
      (name (ns-name migration-namespace)))))

(defn
  migration-namespace? [migration-namespace]
  (.startsWith (namespace-name-str migration-namespace) (str (migrate-namespace-prefix) ".")))

(defn
  migration-namespaces []
  (filter migration-namespace? (all-ns)))

(defn
  migration-number-from-namespace [migration-namespace]
  (Integer/parseInt (re-find #"^[0-9]+" (last (string/split (namespace-name-str migration-namespace) #"\.")))))

(defn 
#^{ :doc "Returns all of the migration files as a collection." }
  all-migration-files
  ([] (all-migration-files (find-migrate-directory)))
  ([migrate-directory]
    (if migrate-directory
      (filter 
        (fn [migrate-file] 
          (re-find #"^[0-9]+_.+\.clj$" (. migrate-file getName))) 
        (.listFiles migrate-directory)))))

(defn 
#^{ :doc "Returns all of the migration file names as a collection." }
  all-migration-file-names 
  ([] (all-migration-file-names (find-migrate-directory)))
  ([migrate-directory]
    (if migrate-directory
      (map 
        (fn [migration-file] (. migration-file getName))
        (all-migration-files migrate-directory)))))
    
(defn
#^{ :doc "Returns the migration number from the given migration file name." }
  migration-number-from-name [migration-file-name]
  (Integer/parseInt (re-find #"^[0-9]+" migration-file-name)))
  
(defn
#^{ :doc "Returns the migration number from the given migration file." }
  migration-number-from-file [migration-file]
  (if migration-file
    (migration-number-from-name (. migration-file getName))))
    
(defn
#^{ :doc "Returns all of the migration file names with numbers between low-number and high-number inclusive." }
  migration-files-in-range [low-number high-number]
  (let [migrate-directory (find-migrate-directory)]
    (filter 
      (fn [migration-file] 
        (let [migration-number (migration-number-from-file migration-file)]
          (and (>= migration-number low-number) (<= migration-number high-number)))) 
      (all-migration-files migrate-directory))))

(defn 
#^{ :doc "Returns all of the numbers prepended to the migration files." }
  all-migration-numbers []
  (map migration-number-from-namespace (migration-namespaces)))

(defn
#^{ :doc "Returns the maximum number of all migration files." }
  max-migration-number []
  (apply max 0 (all-migration-numbers)))

(defn 
#^{ :doc "Returns the next number to use for a migration file." }
  find-next-migrate-number []
  (inc (max-migration-number)))

(defn
#^{ :doc "The migration file with the given migration name." }
  find-migration-file 
  ([migration-name] (find-migration-file (find-migrate-directory) migration-name))
  ([migrate-directory migration-name]
    (let [migration-file-name-to-find (str (dashes-to-underscores migration-name) ".clj")]
      (seq-utils/find-first 
        (fn [migration-file] 
          (re-find 
            (re-pattern (str "[0-9]+_" migration-file-name-to-find))
            (. migration-file getName)))
        (all-migration-files migrate-directory)))))

(defn
#^{:doc "If string ends with the string ending, then remove ending and return the result. Otherwise, return string."}
  strip-ending [string ending]
  (if (and string ending (.endsWith string ending))
    (let [ending-index (- (.length string) (.length ending))]
      (.substring string 0 ending-index))
    string))

(defn
#^{:doc "Converts the given clj file name to a symbol string. For example: \"core.clj\" would get converted into 
\"core\""}
  clj-file-to-symbol-string [file-name]
  (slashes-to-dots (underscores-to-dashes (strip-ending file-name ".clj"))))

(defn
#^{ :doc "Returns a string for the namespace of the given file in the given directory." }
  namespace-string-for-file [file-name]
  (when file-name
    (str (migrate-namespace-prefix) "." (clj-file-to-symbol-string file-name))))

(defn
#^{ :doc "Returns the migration namespace for the given migration file." }
  migration-namespace [migration-file]
  (when migration-file
    (namespace-string-for-file (.getName migration-file))))
  
(defn
#^{ :doc "Finds the number of the migration file before the given number" }
  migration-number-before 
    ([migration-number] 
      (if migration-number 
        (migration-number-before migration-number (all-migration-files))))
    ([migration-number migration-files]
      (when migration-number
        (loop [files migration-files
               previous-file-number 0]
          (if (not-empty migration-files)
            (let [migration-file (first files)
                  migration-file-number (migration-number-from-file migration-file)]
              (if (< migration-file-number migration-number)
                (recur (rest files) migration-file-number)
                previous-file-number))
            previous-file-number)))))