(ns drift.test-execute
  (:use clojure.contrib.test-is
        drift.execute)
  (:require [clojure.contrib.logging :as logging]
            [config.migrate-config :as config]))

(deftest test-version-number
  (is (= 0 (version-number 0)))
  (is (= 1 (version-number "1")))
  (is (= Integer/MAX_VALUE (version-number nil))))

(defn
  test-migrated? [version]
  (is (compare-and-set! config/init-run? true false))
  (is (= version (config/memory-current-version)))) 

(deftest test-migrate
  (compare-and-set! config/init-run? false false)
  (migrate nil [])
  (test-migrated? 2) 
  (migrate 0 [])
  (test-migrated? 0)
  (migrate "1" [])
  (test-migrated? 1)
  (migrate 0 [])
  (test-migrated? 0))

(deftest test-find-version-arg
  (is (= ["0" ()] (find-version-arg ["-version" "0"])))
  (is (= ["0" '("-other" "args")] (find-version-arg ["-version" "0" "-other" "args"])))
  (is (= ["0" '("-other" "args")] (find-version-arg ["-other" "args" "-version" "0"])))
  (is (= ["0" '("-flag")] (find-version-arg ["-flag" "-version" "0"])))
  (is (= [nil ()] (find-version-arg [])))) 

(deftest test-run
  (compare-and-set! config/init-run? false false)
  (run [])
  (test-migrated? 2)
  (run ["-version" "0"])
  (test-migrated? 0)
  (run ["-version" "1" "-other" "args"])
  (test-migrated? 1)
  (run ["-other" "args" "-version" "0"])
  (test-migrated? 0))