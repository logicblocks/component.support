(ns component.support.logging-test
  (:require
    [cartus.core :as ct-core]
    [cartus.test :as ct-test]
    [clojure.test :refer [deftest is]]
    [component.support.logging :as comp-logging]))

(declare logged?)

(deftest logs-before-starting-operation
  (let [action-invocation-time (atom nil)
        test-logger (ct-test/logger)
        timed-logger
        (ct-core/with-transformation test-logger
          (map (fn [event]
                 (assoc event
                   :context
                   (merge (:context event)
                     {:time (System/currentTimeMillis)})))))]
    (comp-logging/with-logging timed-logger :some.thing
      {:phases  {:before :acting :after :acted}
       :context {:some :context}}
      (Thread/sleep 1)
      (reset! action-invocation-time (System/currentTimeMillis))
      (Thread/sleep 1))

    (is (logged? test-logger
          {:level   :debug
           :context {:some :context}
           :type    :some.thing/acting}))
    (let [log-events (ct-test/events test-logger)
          before-event
          (first (filter
                   (fn [event]
                     (= (:type event) :some.thing/acting))
                   log-events))]
      (is (< (get-in before-event [:context :time])
            @action-invocation-time)))))

(deftest logs-after-completing-operation
  (let [action-invocation-time (atom nil)
        test-logger (ct-test/logger)
        timed-logger
        (ct-core/with-transformation test-logger
          (map (fn [event]
                 (assoc event
                   :context
                   (merge (:context event)
                     {:time (System/currentTimeMillis)})))))]
    (comp-logging/with-logging timed-logger :some.thing
      {:phases  {:before :acting :after :acted}
       :context {:some :context}}
      (Thread/sleep 1)
      (reset! action-invocation-time (System/currentTimeMillis))
      (Thread/sleep 1))

    (is (logged? test-logger
          {:level   :info
           :context {:some :context
                     :elapsed-ms number?}
           :type    :some.thing/acted}))
    (let [log-events (ct-test/events test-logger)
          after-event
          (first (filter
                   (fn [event]
                     (= (:type event) :some.thing/acted))
                   log-events))]
      (is (> (get-in after-event [:context :time])
            @action-invocation-time)))))
