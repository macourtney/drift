(ns drift.test-args
  (:use clojure.test drift.args))

(deftest test-split-args
  (is (=  (split-args ["foo" "-v" "bar" "baz"] #{"-v"} )
          [["foo"] ["-v" "bar" "baz"]]))

  (is (=  (split-args ["foo" "bar" "baz"] #{"-v"} )
          [["foo" "bar" "baz"] []])))

(deftest test-remove-opt
  (is (= (remove-opt ["-v" "123" "foo" "bar"] {:key :version :matcher #{"-v"}})
         [{:version "123"} ["foo" "bar"]]))

  (is (= (remove-opt ["-v"] {:key :version :matcher #{"-v"}})
         [{} ["-v"]]))

  (is (= (remove-opt [] {:key :version :matcher #{"-v"}})
         [{} []])))

(deftest test-parse-args
  (is (= (parse-args ["foo" "-v" "10" "bar" "--config" "conf" "baz"]
                     [{:key :version :matcher #{"-v" "--version"}}
                      {:key :config :matcher #{"-v" "--config"}}])
         [{:version "10" :config "conf"} ["foo" "bar" "baz"]])))

(deftest test-parse-migrate-args
  (is (= (parse-migrate-args ["foo" "--other" "blah" "-v" "10" "bar" "--config" "conf" "baz"])
         [{:version "10" :config 'conf} ["foo" "--other" "blah" "bar" "baz"]])))

(deftest test-parse-create-migration-args
  (is (= (parse-create-migration-args ["foo" "--other" "blah" "-v" "10" "bar" "--config" "conf" "baz"])
         [{:config 'conf} ["foo" "--other" "blah" "-v" "10" "bar" "baz"]])))
