(ns drift.test-generator
  (:use clojure.test
        drift.generator
        test-helper)
  (:require config.finished-config
            [drift.builder :as builder]))

(deftest test-migration-usage
  (migration-usage))

(deftest test-create-file-content
  (is (create-file-content "migrations.001-create-tests" nil nil nil)))

(deftest test-generate-migration-file-cmdline
  (with-redefs [drift.generator/generate-migration-file
                (fn [mn]
                  (is (= drift.config/*config-fn-symbol* 'foo.bar/baz))
                  (is (= mn "blahblah")))]

    (generate-migration-file-cmdline
     ["-c" "foo.bar/baz" "blahblah"])))

(deftest test-finished-fn-called
  (with-redefs [builder/find-or-create-migrate-directory (fn [])
                builder/create-migration-file (fn [dir fname])
                drift.generator/generate-file-content (fn [migration-file migration-name ns-content up-content down-content])]

    (generate-migration-file-cmdline ["-c" "config.finished-config/migrate-config" "blahblah"])

    (is (= @config.finished-config/finished-run? true))))
