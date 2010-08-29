(ns drift.test-destroyer
  (:use clojure.contrib.test-is
        drift.destroyer
        test-helper))

(deftest test-migration-usage
  (migration-usage))