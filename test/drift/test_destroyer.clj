(ns drift.test-destroyer
  (:use clojure.test
        drift.destroyer
        test-helper))

(deftest test-migration-usage
  (migration-usage))