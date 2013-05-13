(ns drift.test-config
  (:import [java.io File])
  (:use clojure.test drift.config)
  (:require [test-helper :as test-helper]
            [config.migrate-config :as migrate-config]))

(deftest test-find-config-namespace
  (is (find-config-namespace)))

(deftest test-find-config
  (let [config-map (find-config)]
    (is config-map)
    (is (map? config-map))))

(deftest test-find-config-borks
  (binding [drift.config/*config-fn-symbol* 'config.migrate-config/bar]
    (is (thrown? RuntimeException (find-config)))))

(deftest test-current-version-fn
  (is (= println (current-version-fn {:current-version println })))
  (is (= ((migrate-config/migrate-config) :current-version) (current-version-fn)))
  (is (thrown-with-msg? java.lang.NullPointerException
                        #"Missing .* :current-version"
                        (current-version-fn {}))))

(deftest test-update-version-fn
  (is (= println (update-version-fn {:update-version println })))
  (is (= ((migrate-config/migrate-config) :update-version) (update-version-fn)))
  (is (thrown-with-msg? java.lang.NullPointerException
                        #"Missing .* :update-version"
                        (update-version-fn {}))))

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
  (is (= "/src/migrate" (find-migrate-dir-name {}))))

(deftest test-find-src-dir
  (is (= "/test/" (find-src-dir)))
  (is (= "/test/" (find-src-dir (find-config))))
  (is (= "/something/" (find-src-dir { :directory "/something/somewhere" })))
  (is (= "/src/" (find-src-dir { :src "/src/" } ))))

(deftest test-outer-dir-in-path
  (is (= "/test/" (outer-dir-in-path "/test/migrations")))
  (is (= "test/" (outer-dir-in-path "test/migrations")))
  (is (= "\\test\\" (outer-dir-in-path "\\test\\migrations")))
  (is (= "test\\" (outer-dir-in-path "test\\migrations")))
  (is (nil? (outer-dir-in-path "migrations"))))

