(ns async-kit.event
  (:require [async-kit.core :as ak])
  (:use-macros [async-kite.macros [async let-async]]))

(def ^{ :private true }
  event-handlers (atom {}))

(defn await-event [event-name]
  (async [s e]
         (if (@event-handlers event-name)
           (swap! event-handlers update-in [event-name] conj s)
           (swap! event-handlers assoc event-name [s]))))

(defn emit [event-name data]
  (let [handlers (@event-handlers event-name)]
    (swap! event-handlers dissoc event-name)
    (doseq [handler handlers]
      (handler data))))
