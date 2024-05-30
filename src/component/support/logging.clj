(ns component.support.logging
  (:require
    [cartus.core :as log]))

(defn with-logging-fn [logger target opts action-fn]
  (let [init-ms (System/currentTimeMillis)]
    (when logger
      (log/debug logger (keyword (name target)
                          (name (get-in opts [:phases :before])))
        (:context opts)))
    (let [result (action-fn)]
      (when logger
        (log/info logger (keyword (name target)
                           (name (get-in opts [:phases :after])))
          (merge {:elapsed-ms (- (System/currentTimeMillis) init-ms)}
            (:context opts))))
      result)))

(defmacro with-logging [logger target opts & body]
  `(with-logging-fn ~logger ~target ~opts
     (fn [] ~@body)))
