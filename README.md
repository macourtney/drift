# drift

Drift is a migration library written in Clojure. Drift works much like
Rails migrations where a directory in your project contains all of the
migration files. Drift will detect which migration files need to be
run and run them as appropriate.

## Usage

To use Drift you'll need to add drift to your Leiningen
project. Simply add the following to your project.clj file:

```clojure
[drift "x.x.x"]
```

Where "x.x.x" is the latest version of drift which you can find on
clojars: http://clojars.org/drift

If you want to use the lein commands, you'll need to add the above
vector to your `:dev-dependencies` vector. If you want to access drift
code directly in your project, you'll need to add it to your
`:dependencies` vector. You can add drift to both vectors without any
problems.

To set your Drift migration directory, simply add a clojure file
called "migrate_config.clj" to the directory "src/config".

Your migrate_config.clj file should look something like:

```clojure
 (ns config.migrate-config)

 (defn migrate-config []
   { :directory "/test/migrations"
     :current-version current-db-version-fn
     :update-version update-db-version-fn })
```

`current-db-version-fn` and `update-db-version-fn` are both functions
which you must implement to let Drift read and set the current db
version.

`current-db-version-fn` does not take any parameters, and returns the
current version of the database. If no version has been set, then
`current-db-version-fn` must return 0.

`update-db-version-fn` takes only a new-version parameter. After
`update-db-version-fn` is run with a given new-version,
`current-db-version-fn` must return that version.

For example, here is an in memory db version (note, you do not want to
do this):

```clojure
 (ns config.migrate-config)
 
 (def version (atom nil))
 
 (defn memory-current-db-version []
   (or @version 0)) 
 
 (defn memory-update-db-version [new-version]
   (reset! version new-version))
 
 (defn migrate-config []
   { :directory "/test/migrations"
     :current-db-version memory-current-db-version
     :update-db-version memory-update-db-version })
```

Here is an example database version using a table named
"schema_migrations" that has one version column with a single value
holding the current database version:

```clojure
(ns config.migrate-config
  (:require [clojure.contrib.sql :as sql])
  (:use warehouse.core))

(defn db-version []
  (sql/with-connection DB
    (sql/with-query-results res 
      ["select version from schema_migrations limit 1"]
      (:version (first res)))))

(defn update-db-version [version]
  (sql/with-connection DB
    (sql/insert-values :schema_migrations [:version] [version])))
```

## Initialization

If you need to run some initialization code, add `:init` to your
migrate-config. For example:

```clojure
(defn migrate-config []
   { :directory "/test/migrations"
     :init init
     :current-db-version memory-current-db-version
     :update-db-version memory-update-db-version })
```

The above `migrate-config` will call the function "init" before any
migrations are run. If you pass parameters to `lein migrate`, they
will be passed along to the init function.

If you want to include a special use or require section in the
namespace function of all migration files you can use the `:ns-content`
key.

For example:

```clojure
(defn migrate-config []
   { :directory "/test/migrations"
     :ns-content "\n  (:use database.util)"
     :current-db-version memory-current-db-version
     :update-db-version memory-update-db-version })
```

The above `migrate-config` will add "`\n (:use database.util)`" to the
namespace function call at the top of every migration file.

## Using Leiningen

To migrate to the most recent version:

```bash
$ lein migrate
```

To migrate to a specific version, pass the version as a parameter to
migrate. For example, to migrate to version 1:

```bash
$ lein migrate -version 1
```

To undo all migrations and start with a clean database, simply pass 0
as the migration number. For example:

```bash
$ lein migrate -version 0
```

To create a new migration file which you can then edit:

```bash
$ lein create-migration <migration name>
```

## License

Copyright (C) 2009 Matthew Courtney and released under the Apache 2.0
license.
