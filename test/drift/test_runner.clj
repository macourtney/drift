(ns drift.test-runner
  (:use clojure.test
        drift.runner)
  (:require [drift.core :as core]
            [drift.listener-protocol :as listener-protocol]
            [drift.version :as version]
            [test-helper :as test-helper])
  (:import [drift.listener_protocol ListenerProtocol]))

(def first-migration "create-tests")
(def second-migration "tests-update")

(defn reset-db [test-fn]
  (test-fn)
  (migrate-down-all))

(use-fixtures :once reset-db)

(deftest test-run-migrate-up
  (is (= 0 (version/current-db-version)))
  (run-migrate-up (core/find-migration-namespace first-migration))
  (is (= 1 (version/current-db-version)))
  (version/update-db-version 0)
  (is (= 0 (version/current-db-version)))
  (run-migrate-up nil)
  (is (= 0 (version/current-db-version))))
  
(deftest test-run-migrate-down
  (is (= 0 (version/current-db-version)))
  (run-migrate-up (core/find-migration-namespace first-migration))
  (is (= 1 (version/current-db-version)))
  (run-migrate-down (core/find-migration-namespace first-migration))
  (is (= 0 (version/current-db-version)))
  (run-migrate-down nil)
  (is (= 0 (version/current-db-version)))
  (run-migrate-down (core/find-migration-namespace first-migration))
  (is (= 0 (version/current-db-version))))
  
(deftest test-migrate-up-all
  (is (= 0 (version/current-db-version)))
  (migrate-up-all (core/migration-namespaces))
  (is (= 2 (version/current-db-version)))
  (version/update-db-version 0)
  (is (= 0 (version/current-db-version)))
  (migrate-up-all)
  (is (= 2 (version/current-db-version)))
  (version/update-db-version 0)
  (is (= 0 (version/current-db-version)))
  (migrate-up-all nil)
  (is (= 0 (version/current-db-version))))
  
(deftest test-migrate-down-all
  (is (= 0 (version/current-db-version)))
  (migrate-up-all (core/migration-namespaces))
  (is (= 2 (version/current-db-version)))
  (migrate-down-all (reverse (core/migration-namespaces)))
  (is (= 0 (version/current-db-version)))
  (migrate-up-all (core/migration-namespaces))
  (is (= 2 (version/current-db-version)))
  (migrate-down-all)
  (is (= 0 (version/current-db-version)))
  (migrate-down-all nil)
  (is (= 0 (version/current-db-version))))
  
(deftest test-migrate-up
  (is (= 0 (version/current-db-version)))
  (migrate-up 0 1)
  (is (= 1 (version/current-db-version)))
  (version/update-db-version 0)
  (is (= 0 (version/current-db-version)))
  (migrate-up nil 1)
  (is (= 0 (version/current-db-version)))
  (migrate-up 0 nil)
  (is (= 0 (version/current-db-version)))
  (migrate-up nil nil)
  (is (= 0 (version/current-db-version))))
  
(deftest test-migrate-down
  (is (= 0 (version/current-db-version)))
  (migrate-up 0 1)
  (is (= 1 (version/current-db-version)))
  (migrate-down 1 0)
  (is (= 0 (version/current-db-version)))
  (migrate-down nil 0)
  (is (= 0 (version/current-db-version)))
  (migrate-down 1 nil)
  (is (= 0 (version/current-db-version)))
  (migrate-down nil nil)
  (is (= 0 (version/current-db-version))))
  
(deftest test-update-to-version
  (is (= 0 (version/current-db-version)))
  (update-to-version 1)
  (is (= 1 (version/current-db-version)))
  (update-to-version 2)
  (is (= 2 (version/current-db-version)))
  (update-to-version 3)
  (is (= 2 (version/current-db-version)))
  (update-to-version 1)
  (is (= 1 (version/current-db-version)))
  (update-to-version 0)
  (is (= 0 (version/current-db-version)))
  (update-to-version nil)
  (is (= 0 (version/current-db-version))))
  
(def test-listener (reify ListenerProtocol
                     (start [this namespaces up?] )
                     (running [this namespace up?] )
                     (end [this] )))
  
(deftest test-listeners
  (is (empty? (runner-listeners)))
  (add-listener test-listener)
  (is (= (runner-listeners) [test-listener]))
  (remove-listener test-listener)
  (is (empty? (runner-listeners))))