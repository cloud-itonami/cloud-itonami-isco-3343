(ns execsecretary.actor-test
  (:require [clojure.test :refer [deftest is testing]]
            [execsecretary.actor :as actor]
            [execsecretary.store :as store]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-executive! st {:executive-id "exec-1" :name "Kobo Executive Office"
                                    :max-supply-order-cost 500})
    st))

(deftest commits-a-registered-correspondence-log
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:executive-id "exec-1" :op :log-correspondence-record :stake :low
                 :correspondence-id "C-1" :document-attached? true}
        result (actor/run-request! graph request {} "thread-1")]
    (is (= :done (:status result)))
    (is (some? (get-in result [:state :record])))
    (is (= 1 (count (store/records-of st "exec-1"))))))

(deftest holds-a-request-for-an-unregistered-executive
  (testing "the executive/office record must be independently verified/registered before any action"
    (let [st (fresh-store)
          graph (actor/build-graph {:store st})
          request {:executive-id "ghost" :op :log-correspondence-record :stake :low
                   :correspondence-id "C-1" :document-attached? true}
          result (actor/run-request! graph request {} "thread-2")]
      (is (= :hold (:disposition (:state result))))
      (is (empty? (store/records-of st "ghost"))))))

(deftest interrupts-then-approves-a-flag-authority-concern-on-human-approval
  (testing "flag-authority-concern is never auto-commit-eligible — it always
            interrupts for human sign-off regardless of confidence"
    (let [st (fresh-store)
          graph (actor/build-graph {:store st})
          request {:executive-id "exec-1" :op :flag-authority-concern :stake :low
                   :concern-type :pending-signature-request}
          interrupted (actor/run-request! graph request {} "thread-3")]
      (is (= :interrupted (:status interrupted)))
      (is (empty? (store/records-of st "exec-1")))
      (let [resumed (actor/approve! graph "thread-3")]
        (is (= :done (:status resumed)))
        (is (= 1 (count (store/records-of st "exec-1"))))))))

(deftest interrupts-then-approves-a-supply-order-above-cost-ceiling
  (testing "a supply order above the executive's registered cost ceiling escalates
            (routine procurement, not a hard block) but still requires human
            sign-off before it is ever committed"
    (let [st (fresh-store)
          graph (actor/build-graph {:store st})
          request {:executive-id "exec-1" :op :coordinate-supply-order :stake :low
                   :supply-order-id "S-1" :cost 5000}
          interrupted (actor/run-request! graph request {} "thread-4")]
      (is (= :interrupted (:status interrupted)))
      (is (empty? (store/records-of st "exec-1")))
      (let [resumed (actor/approve! graph "thread-4")]
        (is (= :done (:status resumed)))
        (is (= 1 (count (store/records-of st "exec-1"))))))))
