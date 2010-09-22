(ns drift.test-core
  (:import [java.io File])
  (:use clojure.contrib.test-is
        drift.core)
  (:require [clojure.contrib.logging :as logging]
            [config.migrate-config :as migrate-config]))

(def migration-name "create-tests")

(deftest test-find-config-namespace
  (is (find-config-namespace)))

(deftest test-find-config
  (let [config-map (find-config)]
    (is config-map)
    (is (map? config-map))))

(deftest test-find-init-fn
  (is (= migrate-config/init (find-init-fn))))

(deftest test-default-ns-content
  (is (= "\n  (:use clojure.contrib.sql)" (default-ns-content))))

(deftest test-find-migrate-dir-name
  (let [migrate-dir-name (find-migrate-dir-name)]
    (is migrate-dir-name)
    (is (string? migrate-dir-name))
    (is (= "/test/migrations" migrate-dir-name))))

(deftest test-migrate-directory
  (let [migrate-dir (migrate-directory)]
    (is migrate-dir)
    (is (instance? File migrate-dir))))

(deftest test-find-migrate-directory
  (let [migrate-dir (find-migrate-directory)]
    (is migrate-dir)
    (is (instance? File migrate-dir))))

(deftest test-file-separator-index
  (is (= 5 (file-separator-index "/test/migrations")))
  (is (= 4 (file-separator-index "test/migrations")))
  (is (= 5 (file-separator-index "\\test\\migrations")))
  (is (= 4 (file-separator-index "test\\migrations")))
  (is (nil? (file-separator-index "migrations")))
  (is (nil? (file-separator-index nil))))

(deftest test-migrate-namespace-prefix-from-directory
  (is (= "migrations" (migrate-namespace-prefix-from-directory)))
  (is (= "migrations" (migrate-namespace-prefix-from-directory "/test/migrations")))
  (is (= "db.migrate" (migrate-namespace-prefix-from-directory "/test/db/migrate")))
  (is (= "db.migrate" (migrate-namespace-prefix-from-directory "test/db/migrate")))
  (is (= "migrate" (migrate-namespace-prefix-from-directory "migrate")))
  (is (nil? (migrate-namespace-prefix-from-directory nil))))

(deftest test-migration-namespaces
  (let [migration-namespaces (migration-namespaces)]
    (is (= 2 (count migration-namespaces)))
    (is (= ["migrations.001-create-tests" "migrations.002-test-update"] (map namespace-name-str migration-namespaces)))))

(deftest test-migration-number-from-namespace
  (is (= 1 (migration-number-from-namespace "migrations.001-create-tests")))
  (is (= 2 (migration-number-from-namespace "migrations.002-test-update"))))

(deftest test-find-migrate-directory
  (let [migrate-directory (find-migrate-directory)]
    (is (not (nil? migrate-directory)))
    (when migrate-directory
      (is (= "migrations" (.getName migrate-directory))))))
    
(deftest test-all-migration-files
  (let [all-migrations (all-migration-files (find-migrate-directory))]
    (is (not (nil? all-migrations)))
    (is (seq? all-migrations))
    (is (not-empty all-migrations))
    (is (instance? File (first all-migrations))))
  (is (not (nil? (all-migration-files))))
  (is (nil? (all-migration-files nil))))
  
(deftest test-all-migration-file-names
  (let [all-migration-names (all-migration-file-names (find-migrate-directory))]
    (is (not (nil? all-migration-names)))
    (is (seq? all-migration-names))
    (is (not-empty all-migration-names))
    (is (instance? String (first all-migration-names))))
  (is (not (nil? (all-migration-file-names))))
  (is (nil? (all-migration-file-names nil))))

(deftest test-migration-number-from-file
  (let [migration-file (new File "001-create-test.clj")
        migration-number (migration-number-from-file migration-file)]
    (is (= migration-number 1)))
  (let [migration-file (new File "002-create-test.clj")
        migration-number (migration-number-from-file migration-file)]
    (is (= migration-number 2)))
  (let [migration-file (new File "000-create-test.clj")
        migration-number (migration-number-from-file migration-file)]
    (is (= migration-number 0)))
  (is (thrown? NumberFormatException (migration-number-from-file (new File "create-test.clj"))))
  (is (nil? (migration-number-from-file nil))))

(deftest test-migration-files-in-range
  (let [migration-files (migration-files-in-range 0 1)]
    (is (not-empty migration-files)))
  (let [migration-files (migration-files-in-range 0 0)]
    (is (empty migration-files))))

(deftest test-all-migration-numbers
  (let [migration-numbers (all-migration-numbers)]
    (is (not-empty migration-numbers))
    (is (number? (first migration-numbers)))
    (is (= [1 2]  migration-numbers))))

(deftest test-max-migration-number
  (let [max-number (max-migration-number)]
    (is (= max-number 2))))

(deftest test-find-next-migrate-number
  (is (= (find-next-migrate-number) (inc (max-migration-number)))))

(deftest test-find-migration-file
  (let [migration-file (find-migration-file (find-migrate-directory) migration-name)]
    (is (not (nil? migration-file)))
    (is (instance? File migration-file)))
  (let [migration-file (find-migration-file migration-name)]
    (is (not (nil? migration-file)))
    (is (instance? File migration-file)))
  (is (nil? (find-migration-file nil))))
  
(deftest test-migration-namespace
  (let [migration-ns (migration-namespace (find-migration-file migration-name))]
    (is (not (nil? migration-ns)))
    (is (= (str "migrations.001-" migration-name) migration-ns)))
  (is (nil? (migration-namespace nil))))
  
(deftest test-migration-number-before
  (let [migration-number (migration-number-before 1 (all-migration-files))]
    (is (= 0 migration-number)))
  (let [migration-number (migration-number-before 1)]
    (is (= 0 migration-number)))
  (is (nil? (migration-number-before nil)))
  (is (nil? (migration-number-before nil (all-migration-files)))))