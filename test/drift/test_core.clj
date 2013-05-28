(ns drift.test-core
  (:import [java.io File])
  (:use clojure.test
        drift.core)
  (:require [test-helper :as test-helper]))

(def migration-name "create-tests")

(deftest test-migrate-directory
  (let [migrate-dir (migrate-directory)]
    (is migrate-dir)
    (is (instance? File migrate-dir))))

(deftest test-find-migrate-directory
  (let [migrate-dir (find-migrate-directory)]
    (is migrate-dir)
    (is (instance? File migrate-dir))))

(deftest test-migrate-namespace-prefix-from-directory
  (is (= "migrations" (migrate-namespace-prefix-from-directory)))
  (is (= "migrations" (migrate-namespace-prefix-from-directory "/test/migrations")))
  (is (= "db.migrate" (migrate-namespace-prefix-from-directory "/test/db/migrate")))
  (is (nil? (migrate-namespace-prefix-from-directory nil))))

(deftest test-sort-migration-namespaces
  (is (= (sort-migration-namespaces ["migrations.001-create-tests" "migrations.002-test-update"])
         ["migrations.001-create-tests" "migrations.002-test-update"]))
  (is (= (sort-migration-namespaces ["migrations.002-test-update" "migrations.001-create-tests"])
         ["migrations.001-create-tests" "migrations.002-test-update"]))
  (is (= 
        (sort-migration-namespaces
          ["imsma.database.migrations.20111113100406-init-for-clojure", 
           "imsma.database.migrations.20120306100918-add-poly-prop-fields", 
           "imsma.database.migrations.20120327161904-fix-gazetteer-fields", 
           "imsma.database.migrations.20121203164415-update-uncategorised", 
           "imsma.database.migrations.20121205100302-add-field-report-sub-object-fields", 
           "imsma.database.migrations.20130409102159-create-reconciliation-update-type", 
           "imsma.database.migrations.20110800000010-create-link-to-index", 
           "imsma.database.migrations.20130102104441-add-linked-ids-fields", 
           "imsma.database.migrations.20130510133842-add-assistance-tables", 
           "imsma.database.migrations.20110600000070-change-custom-view-fonts", 
           "imsma.database.migrations.20110800000020-expand-preference-key-column", 
           "imsma.database.migrations.20120223153548-poly-property-cleanup", 
           "imsma.database.migrations.20120312143040-add-poly-props-enums", 
           "imsma.database.migrations.20120326165854-fix-geospatialinfo-fields", 
           "imsma.database.migrations.20120327163802-set-version-5-8-3", 
           "imsma.database.migrations.20130226142811-fix-missing-and-duplicate-categories", 
           "imsma.database.migrations.20130423161211-update-organisation-category", 
           "imsma.database.migrations.20130527111619-add-deleted-current-view-guid-to-assistance-version", 
           "imsma.database.migrations.20110600000030-update-cdfvalue-types", 
           "imsma.database.migrations.20110600000040-update-field-table-fieldtype-column-width", 
           "imsma.database.migrations.20110600000080-update-link-table", 
           "imsma.database.migrations.20120326134444-remove-poly-property", 
           "imsma.database.migrations.20120420142920-update-geopoint-precision", 
           "imsma.database.migrations.20130121093438-create-classification-base-data", 
           "imsma.database.migrations.20110600000050-adds-values-to-category-table", 
           "imsma.database.migrations.20120105111122-geopoint-precision", 
           "imsma.database.migrations.20120410180758-fix-null-imsmaenum-dataenterers", 
           "imsma.database.migrations.20120516171019-update-mimetype-length", 
           "imsma.database.migrations.20121010133359-set-version-6-0-0", 
           "imsma.database.migrations.20130117154649-add-object-ids-fields", 
           "imsma.database.migrations.20130118095826-create-classification-level-tables", 
           "imsma.database.migrations.20130419151756-add-missing-search-fields", 
           "imsma.database.migrations.20130514121330-add-deleted-current-view-guid", 
           "imsma.database.migrations.20130524144040-add-contains-assistance-version-to-field-report-desc", 
           "imsma.database.migrations.20110600000060-add-values-to-field-table", 
           "imsma.database.migrations.20111100000010-add-field-report-submit-permission", 
           "imsma.database.migrations.20130419142709-update-travel-time-fields", 
           "imsma.database.migrations.20130424130139-add-field-column-to-translation", 
           "imsma.database.migrations.20130424142608-user-entered-coordinates", 
           "imsma.database.migrations.20130506163750-remove-sector-and-sample", 
           "imsma.database.migrations.20111123101028-set-version-5-9-0", 
           "imsma.database.migrations.20121203143146-add-field-report-report-verified-date-field", 
           "imsma.database.migrations.20130418105528-update-mre-type-field", 
           "imsma.database.migrations.20110800000030-update-decimal-handling", 
           "imsma.database.migrations.20121010133408-update-is-active-fields", 
           "imsma.database.migrations.20130515143404-add-version-sub-object-fields", 
           "imsma.database.migrations.20110600000090-update-float-fields", 
           "imsma.database.migrations.20111123101344-add-poly-property", 
           "imsma.database.migrations.20130329095946-add-object-filtering-to-fieldreportdesc"])
         ["imsma.database.migrations.20110600000030-update-cdfvalue-types", 
          "imsma.database.migrations.20110600000040-update-field-table-fieldtype-column-width", 
          "imsma.database.migrations.20110600000050-adds-values-to-category-table",
          "imsma.database.migrations.20110600000060-add-values-to-field-table", 
          "imsma.database.migrations.20110600000070-change-custom-view-fonts", 
          "imsma.database.migrations.20110600000080-update-link-table",
          "imsma.database.migrations.20110600000090-update-float-fields", 
          "imsma.database.migrations.20110800000010-create-link-to-index",
          "imsma.database.migrations.20110800000020-expand-preference-key-column",
          "imsma.database.migrations.20110800000030-update-decimal-handling", 
          "imsma.database.migrations.20111100000010-add-field-report-submit-permission", 
          "imsma.database.migrations.20111113100406-init-for-clojure", 
          "imsma.database.migrations.20111123101028-set-version-5-9-0", 
          "imsma.database.migrations.20111123101344-add-poly-property", 
          "imsma.database.migrations.20120105111122-geopoint-precision",
          "imsma.database.migrations.20120223153548-poly-property-cleanup", 
          "imsma.database.migrations.20120306100918-add-poly-prop-fields", 
          "imsma.database.migrations.20120312143040-add-poly-props-enums", 
          "imsma.database.migrations.20120326134444-remove-poly-property", 
          "imsma.database.migrations.20120326165854-fix-geospatialinfo-fields", 
          "imsma.database.migrations.20120327161904-fix-gazetteer-fields", 
          "imsma.database.migrations.20120327163802-set-version-5-8-3", 
          "imsma.database.migrations.20120410180758-fix-null-imsmaenum-dataenterers", 
          "imsma.database.migrations.20120420142920-update-geopoint-precision", 
          "imsma.database.migrations.20120516171019-update-mimetype-length", 
          "imsma.database.migrations.20121010133359-set-version-6-0-0", 
          "imsma.database.migrations.20121010133408-update-is-active-fields", 
          "imsma.database.migrations.20121203143146-add-field-report-report-verified-date-field", 
          "imsma.database.migrations.20121203164415-update-uncategorised", 
          "imsma.database.migrations.20121205100302-add-field-report-sub-object-fields",
          "imsma.database.migrations.20130102104441-add-linked-ids-fields",
          "imsma.database.migrations.20130117154649-add-object-ids-fields",
          "imsma.database.migrations.20130118095826-create-classification-level-tables", 
          "imsma.database.migrations.20130121093438-create-classification-base-data",
          "imsma.database.migrations.20130226142811-fix-missing-and-duplicate-categories",
          "imsma.database.migrations.20130329095946-add-object-filtering-to-fieldreportdesc"
          "imsma.database.migrations.20130409102159-create-reconciliation-update-type", 
          "imsma.database.migrations.20130418105528-update-mre-type-field", 
          "imsma.database.migrations.20130419142709-update-travel-time-fields", 
          "imsma.database.migrations.20130419151756-add-missing-search-fields", 
          "imsma.database.migrations.20130423161211-update-organisation-category", 
          "imsma.database.migrations.20130424130139-add-field-column-to-translation", 
          "imsma.database.migrations.20130424142608-user-entered-coordinates",
          "imsma.database.migrations.20130506163750-remove-sector-and-sample", 
          "imsma.database.migrations.20130510133842-add-assistance-tables", 
          "imsma.database.migrations.20130514121330-add-deleted-current-view-guid",
          "imsma.database.migrations.20130515143404-add-version-sub-object-fields", 
          "imsma.database.migrations.20130524144040-add-contains-assistance-version-to-field-report-desc",
          "imsma.database.migrations.20130527111619-add-deleted-current-view-guid-to-assistance-version"])))

(deftest test-migration-namespaces
  (let [migration-namespaces (migration-namespaces)]
    (is (= 2 (count migration-namespaces)))
    (is (= ["migrations.001-create-tests" "migrations.002-test-update"] (map namespace-name-str migration-namespaces))))
  (is (= ["migrations.002-test-update" "migrations.001-create-tests"] (map namespace-name-str (migration-namespaces false)))))

(deftest test-migration-number-from-namespace
  (is (= 1 (migration-number-from-namespace "migrations.001-create-tests")))
  (is (= 2 (migration-number-from-namespace "migrations.002-test-update")))
  (is (= 20130527111619 (migration-number-from-namespace "database.migrations.20130527111619-test-long-name")))
  (is (= 20111113100406 (migration-number-from-namespace "imsma.database.migrations.20111113100406-init-for-clojure")))
  (is (= 20110600000030 (migration-number-from-namespace "imsma.database.migrations.20110600000030-update-cdfvalue-types"))))

(deftest test-migration-compartor
  (let [ascending-migration-compartor (migration-compartor true)]
    (is (< (.compare ascending-migration-compartor "imsma.database.migrations.20110600000030-update-cdfvalue-types" "imsma.database.migrations.20111113100406-init-for-clojure")))
    (is (> (.compare ascending-migration-compartor "imsma.database.migrations.20111113100406-init-for-clojure" "imsma.database.migrations.20110600000030-update-cdfvalue-types"))))
  (let [descending-migration-compartor (migration-compartor false)]
    (is (> (.compare descending-migration-compartor "imsma.database.migrations.20110600000030-update-cdfvalue-types" "imsma.database.migrations.20111113100406-init-for-clojure")))
    (is (< (.compare descending-migration-compartor "imsma.database.migrations.20111113100406-init-for-clojure" "imsma.database.migrations.20110600000030-update-cdfvalue-types")))))

(deftest test-find-migrate-directory
  (let [migrate-directory (find-migrate-directory)]
    (is (not (nil? migrate-directory)))
    (when migrate-directory
      (is (= "migrations" (.getName migrate-directory))))))

(deftest test-migration-namespaces-in-range
  (let [migration-namespaces (migration-namespaces-in-range 0 1)]
    (is (not-empty migration-namespaces))
    (is (= migration-namespaces ["migrations.001-create-tests"])))
  (let [migration-namespaces (migration-namespaces-in-range 0 2)]
    (is (not-empty migration-namespaces))
    (is (= migration-namespaces ["migrations.001-create-tests" "migrations.002-test-update"])))
  (let [migration-namespaces (migration-namespaces-in-range 0 0)]
    (is (empty? migration-namespaces))))

(deftest test-all-migration-numbers
  (let [migration-numbers (migration-numbers)]
    (is (not-empty migration-numbers))
    (is (number? (first migration-numbers)))
    (is (= [1 2]  migration-numbers)))
  (let [migration-numbers (migration-numbers ["migrations.001-test" "migrations.002-test" "migrations.005-test"])]
    (is (not-empty migration-numbers))
    (is (number? (first migration-numbers)))
    (is (= [1 2 5]  migration-numbers))))

(deftest test-max-migration-number
  (let [max-number (max-migration-number)]
    (is (= max-number 2)))
  (let [max-number (max-migration-number (migration-namespaces))]
    (is (= max-number 2)))
  (let [max-number (max-migration-number ["migrations.001-test" "migrations.002-test" "migrations.005-test"])]
    (is (= max-number 5))))

(deftest test-find-next-migrate-number
  (is (= (find-next-migrate-number) (inc (max-migration-number)))))

(deftest test-migration-number-before
  (let [migration-number (migration-number-before 1 (migration-namespaces))]
    (is (= 0 migration-number)))
  (let [migration-number (migration-number-before 1)]
    (is (= 0 migration-number)))
  (is (nil? (migration-number-before nil)))
  (is (nil? (migration-number-before nil (migration-namespaces)))))

(deftest test-find-migration-file
  (let [migration-file (find-migration-file "create-tests")]
    (is migration-file)
    (when migration-file
      (is (instance? File migration-file))
      (is (= "001_create_tests.clj" (.getName migration-file)))
      (is (= (find-migrate-directory) (.getParentFile migration-file))))))