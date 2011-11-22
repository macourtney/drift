(ns drift.core
  (:import [java.io File]
           [java.util Comparator])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as logging]
            [clojure.tools.loading-utils :as loading-utils]))

(def migrate-dir "migrate")

(def config-ns-symbol 'config.migrate-config)

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
  migrate-namespace-dir
  ([] (migrate-namespace-dir (find-migrate-dir-name)))
  ([migrate-dir-name]
    (if-let [file-separator-index (file-separator-index migrate-dir-name)]
      (.substring migrate-dir-name (inc file-separator-index))
       migrate-dir-name)))

(defn
#^{ :doc "Returns the namespace prefix for the migrate directory name." }
  migrate-namespace-prefix-from-directory
  ([] (migrate-namespace-prefix-from-directory (find-migrate-dir-name)))
  ([migrate-dir-name]
    (loading-utils/slashes-to-dots (loading-utils/underscores-to-dashes (migrate-namespace-dir migrate-dir-name)))))

(defn
  migrate-namespace-prefix []
  (or (:namespace-prefix (find-config)) (migrate-namespace-prefix-from-directory)))

(defn
#^{ :doc "Returns a string for the namespace of the given file in the given directory." }
  namespace-string-for-file [file-name]
  (when file-name
    (str (migrate-namespace-prefix) "." (loading-utils/clj-file-to-symbol-string file-name))))

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
  (if-let [migration-namespaces (:migration-namespaces (find-config))]
    (migration-namespaces (find-migrate-dir-name) (migrate-namespace-prefix))
    (map namespace-string-for-file
      (filter #(re-matches #".*\.clj$" %)
        (loading-utils/all-class-path-file-names (migrate-namespace-dir))))))

(defn
  migration-number-from-namespace [migration-namespace]
  (when migration-namespace
    (when-let [migration-number-str (re-find #"^[0-9]+" (last (string/split (namespace-name-str migration-namespace) #"\.")))]
      (Long/parseLong migration-number-str))))

(defn
#^{ :doc "Returns all of the migration file names with numbers between low-number and high-number inclusive." }
  migration-namespaces-in-range [low-number high-number]
  (sort
    (reify Comparator
      (compare [this namespace1 namespace2]
        (if (< low-number high-number)
          (- (migration-number-from-namespace namespace1) (migration-number-from-namespace namespace2))
          (- (migration-number-from-namespace namespace2) (migration-number-from-namespace namespace1))))
      (equals [this object] (= this object)))
    (filter 
      (fn [migration-namespace] 
        (let [migration-number (migration-number-from-namespace migration-namespace)]
          (and (>= migration-number low-number) (<= migration-number high-number)))) 
      (migration-namespaces))))

(defn 
#^{ :doc "Returns all of the numbers prepended to the migration files." }
  migration-numbers
  ([] (migration-numbers (migration-namespaces)))
  ([migration-namespaces]
    (filter identity (map migration-number-from-namespace migration-namespaces))))

(defn max-migration-number
  "Returns the maximum number of all migration files."
  ([migration-namespaces] (reduce max 0 (migration-numbers migration-namespaces)))
  ([] (reduce max 0 (migration-numbers))))

(defn 
#^{ :doc "Returns the next number to use for a migration file." }
  find-next-migrate-number []
  (inc (max-migration-number)))

(defn
#^{ :doc "Finds the number of the migration file before the given number" }
  migration-number-before 
  ([migration-number] (migration-number-before migration-number (migration-namespaces)))
  ([migration-number migration-namespaces]
    (when migration-number
      (apply max 0 (filter #(< %1 migration-number) (migration-numbers migration-namespaces))))))

(defn
  find-migration-namespace [migration-name]
  (some
    #(when (re-find (re-pattern (str (migrate-namespace-prefix) "\\.[0-9]+-" migration-name)) %1) %1)
    (map namespace-name-str (migration-namespaces))))

(defn
#^{ :doc "The migration file with the given migration name." }
  find-migration-file
  ([migration-name] (find-migration-file (find-migrate-directory) migration-name))
  ([migrate-directory migration-name]
    (when-let [namespace-str (find-migration-namespace migration-name)]
        (File. migrate-directory (.getName (File. (loading-utils/symbol-string-to-clj-file namespace-str)))))))

(defn
#^{ :doc "Returns the migration namespace for the given migration file." }
  migration-namespace [migration-file]
  (when migration-file
    (namespace-string-for-file (.getName migration-file))))