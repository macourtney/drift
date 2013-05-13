(ns drift.test-generator
  (:use clojure.test
        drift.generator
        test-helper))

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
