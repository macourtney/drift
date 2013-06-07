(ns drift.test-execute
  (:use clojure.test
        drift.execute)
  (:require [config.migrate-config :as config]
            config.finished-config
            [test-helper :as test-helper]))

(deftest test-version-number
  (is (= 0 (version-number 0)))
  (is (= 1 (version-number "1")))
  (is (= Long/MAX_VALUE (version-number nil))))

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

(deftest test-run-custom-config
  (with-redefs [drift.execute/migrate
                (fn [version remaining-args]
                  (is (= version "1234"))
                  (is (= drift.config/*config-fn-symbol* 'foo.bar/baz))
                  (is (= remaining-args ["bloop" "blargh"])))]
    (run ["-version" "1234" "bloop" "-c" "foo.bar/baz" "blargh"])))

(deftest test-run-dynamic-config
  (with-redefs [drift.runner/update-to-version
                (fn [version]
                  (is (= version 1234))
                  (is (= 42 (:more-config drift.config/*config-map*))))]

    (run ["-version" "1234" "bloop" "-c" "config.dynamic-config/config" "blargh"])))

(deftest test-finished-fn-called
  []
  (with-redefs [drift.runner/update-to-version (fn [version])]
    (run ["-version" "1234" "bloop" "-c" "config.finished-config/migrate-config" "blargh"])

   (is (= @config.finished-config/finished-run? true))))
