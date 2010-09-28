(ns drift.test-generator
  (:use clojure.contrib.test-is
        drift.generator
        test-helper))

(deftest test-migration-usage
  (migration-usage))

(deftest test-create-file-content
  (is (create-file-content "migrations.001-create-tests" nil nil nil)))