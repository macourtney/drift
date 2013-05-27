(ns drift.listener-protocol)

(defprotocol ListenerProtocol
  (start [this namespaces ^Boolean up?] "Called before drift starts executing migrations. The given namespaces is a list of all namespaces of the migrations to run.")
  (running [this ^String namespace ^Boolean up?] "Called for each migration just before it is run. The given namespace is the namespace of the migration to run.")
  (end [this] "Called after all migrations have completed."))