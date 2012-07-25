(ns drift.test-core
  (:import [java.io File])
  (:use clojure.test
        drift.core)
  (:require [config.migrate-config :as migrate-config]
            [test-helper :as test-helper]))

(def migration-name "create-tests")

(deftest test-find-config-namespace
  (is (find-config-namespace)))

(deftest test-find-config
  (let [config-map (find-config)]
    (is config-map)
    (is (map? config-map))))

(deftest test-find-init-fn
  (is (= migrate-config/init (find-init-fn)))
  (is (= migrate-config/init (find-init-fn (find-config))))
  (is (nil? (find-init-fn {}))))

(deftest test-default-ns-content
  (is (= "\n  (:use clojure.contrib.sql)" (default-ns-content)))
  (is (= "\n  (:use clojure.contrib.sql)" (default-ns-content (find-config))))
  (is (nil? (default-ns-content {}))))

(deftest test-find-migrate-dir-name
  (let [migrate-dir-name (find-migrate-dir-name)]
    (is migrate-dir-name)
    (is (string? migrate-dir-name))
    (is (= "/test/migrations" migrate-dir-name)))
  (is (= "/test/migrations" (find-migrate-dir-name (find-config))))
  (is (= migrate-dir (find-migrate-dir-name {}))))

(deftest test-migrate-directory
  (let [migrate-dir (migrate-directory)]
    (is migrate-dir)
    (is (instance? File migrate-dir))))

(deftest test-find-migrate-directory
  (let [migrate-dir (find-migrate-directory)]
    (is migrate-dir)
    (is (instance? File migrate-dir))))

(deftest test-find-src-dir
  (is (= "/test/" (find-src-dir)))
  (is (= "/test/" (find-src-dir (find-config))))
  (is (= "/src/" (find-src-dir { :src "/src/" } ))))

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
  (is (nil? (migrate-namespace-prefix-from-directory nil))))

(deftest test-migration-namespaces
  (let [migration-namespaces (migration-namespaces)]
    (is (= 2 (count migration-namespaces)))
    (is (= ["migrations.001-create-tests" "migrations.002-test-update"] (map namespace-name-str migration-namespaces))))
  (is (= ["migrations.002-test-update" "migrations.001-create-tests"] (map namespace-name-str (migration-namespaces false)))))

(deftest test-migration-number-from-namespace
  (is (= 1 (migration-number-from-namespace "migrations.001-create-tests")))
  (is (= 2 (migration-number-from-namespace "migrations.002-test-update"))))

(deftest test-find-migrate-directory
  (let [migrate-directory (find-migrate-directory)]
    (is (not (nil? migrate-directory)))
    (when migrate-directory
      (is (= "migrations" (.getName migrate-directory))))))

(deftest test-migration-namespaces-in-range
  (let [migration-namespaces (migration-namespaces-in-range 0 1)]
    (is (not-empty migration-namespaces))
    (is (= migration-namespaces ["migrations.001-create-tests"])))
  (let [migration-namespaces (migration-namespaces-in-range 0 2)]
    (is (not-empty migration-namespaces))
    (is (= migration-namespaces ["migrations.001-create-tests" "migrations.002-test-update"])))
  (let [migration-namespaces (migration-namespaces-in-range 0 0)]
    (is (empty migration-namespaces))))

(deftest test-all-migration-numbers
  (let [migration-numbers (migration-numbers)]
    (is (not-empty migration-numbers))
    (is (number? (first migration-numbers)))
    (is (= [1 2]  migration-numbers)))
  (let [migration-numbers (migration-numbers ["migrations.001-test" "migrations.002-test" "migrations.005-test"])]
    (is (not-empty migration-numbers))
    (is (number? (first migration-numbers)))
    (is (= [1 2 5]  migration-numbers))))

(deftest test-max-migration-number
  (let [max-number (max-migration-number)]
    (is (= max-number 2)))
  (let [max-number (max-migration-number (migration-namespaces))]
    (is (= max-number 2)))
  (let [max-number (max-migration-number ["migrations.001-test" "migrations.002-test" "migrations.005-test"])]
    (is (= max-number 5))))

(deftest test-find-next-migrate-number
  (is (= (find-next-migrate-number) (inc (max-migration-number)))))

(deftest test-migration-number-before
  (let [migration-number (migration-number-before 1 (migration-namespaces))]
    (is (= 0 migration-number)))
  (let [migration-number (migration-number-before 1)]
    (is (= 0 migration-number)))
  (is (nil? (migration-number-before nil)))
  (is (nil? (migration-number-before nil (migration-namespaces)))))

(deftest test-find-migration-file
  (let [migration-file (find-migration-file "create-tests")]
    (is migration-file)
    (when migration-file
      (is (instance? File migration-file))
      (is (= "001_create_tests.clj" (.getName migration-file)))
      (is (= (find-migrate-directory) (.getParentFile migration-file))))))