(ns drift.test-runner
  (:use clojure.contrib.test-is
        drift.runner)
  (:require [drift.core :as core]
            [drift.version :as version]))

(def first-migration "create-tests")
(def second-migration "tests-update")

(defn reset-db [test-fn]
  (test-fn)
  (migrate-down-all))

(use-fixtures :once reset-db)

(deftest test-run-migrate-up
  (is (= 0 (version/current-db-version)))
  (run-migrate-up (core/find-migration-file first-migration))
  (is (= 1 (version/current-db-version)))
  (version/update-db-version 0)
  (is (= 0 (version/current-db-version)))
  (run-migrate-up nil)
  (is (= 0 (version/current-db-version))))
  
(deftest test-run-migrate-down
  (is (= 0 (version/current-db-version)))
  (run-migrate-up (core/find-migration-file first-migration))
  (is (= 1 (version/current-db-version)))
  (run-migrate-down (core/find-migration-file first-migration))
  (is (= 0 (version/current-db-version)))
  (run-migrate-down nil)
  (is (= 0 (version/current-db-version)))
  (run-migrate-down first-migration)
  (is (= 0 (version/current-db-version))))
  
(deftest test-migrate-up-all
  (is (= 0 (version/current-db-version)))
  (migrate-up-all (core/all-migration-files))
  (is (= 2 (version/current-db-version)))
  (version/update-db-version 0)
  (is (= 0 (version/current-db-version)))
  (migrate-up-all)
  (is (= 2 (version/current-db-version)))
  (version/update-db-version 0)
  (is (= 0 (version/current-db-version)))
  (migrate-up-all nil true)
  (is (= 0 (version/current-db-version))))
  
(deftest test-migrate-down-all
  (is (= 0 (version/current-db-version)))
  (migrate-up-all (core/all-migration-files) true)
  (is (= 2 (version/current-db-version)))
  (migrate-down-all (reverse (core/all-migration-files)))
  (is (= 0 (version/current-db-version)))
  (migrate-up-all (core/all-migration-files) true)
  (is (= 2 (version/current-db-version)))
  (migrate-down-all)
  (is (= 0 (version/current-db-version)))
  (migrate-down-all nil true)
  (is (= 0 (version/current-db-version))))
  
(deftest test-migrate-up
  (is (= 0 (version/current-db-version)))
  (migrate-up 0 1 true)
  (is (= 1 (version/current-db-version)))
  (version/update-db-version 0)
  (is (= 0 (version/current-db-version)))
  (migrate-up nil 1 true)
  (is (= 0 (version/current-db-version)))
  (migrate-up 0 nil true)
  (is (= 0 (version/current-db-version)))
  (migrate-up nil nil true)
  (is (= 0 (version/current-db-version))))
  
(deftest test-migrate-down
  (is (= 0 (version/current-db-version)))
  (migrate-up 0 1 true)
  (is (= 1 (version/current-db-version)))
  (migrate-down 1 0)
  (is (= 0 (version/current-db-version)))
  (migrate-down nil 0 true)
  (is (= 0 (version/current-db-version)))
  (migrate-down 1 nil true)
  (is (= 0 (version/current-db-version)))
  (migrate-down nil nil true)
  (is (= 0 (version/current-db-version))))
  
(deftest test-update-to-version
  (is (= 0 (version/current-db-version)))
  (update-to-version 1)
  (is (= 1 (version/current-db-version)))
  (update-to-version 2 true)
  (is (= 2 (version/current-db-version)))
  (update-to-version 3 true)
  (is (= 2 (version/current-db-version)))
  (update-to-version 1 true)
  (is (= 1 (version/current-db-version)))
  (update-to-version 0 true)
  (is (= 0 (version/current-db-version)))
  (update-to-version nil true)
  (is (= 0 (version/current-db-version))))