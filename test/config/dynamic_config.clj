(ns config.dynamic-config
  (use clojure.test))

(defn config []

  {:init (fn [args]
           (is (= args ["bloop" "blargh"]))
           {:more-config 42})})
