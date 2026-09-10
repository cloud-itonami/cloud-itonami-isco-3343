(ns execsecretary.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [execsecretary.store :as store]
            [execsecretary.advisor :as advisor]
            [execsecretary.governor :as governor]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-executive! st {:executive-id "exec-1" :name "Kobo Executive Office"
                                    :max-supply-order-cost 500})
    st))

(defn- corr-op [attached?]
  {:op :log-correspondence-record :effect :propose :executive-id "exec-1"
   :correspondence-id "C-1" :document-attached? attached?
   :confidence 0.9 :stake :low
   :rationale "proposed log-correspondence-record for executive exec-1"})

(defn- supply-op [cost]
  {:op :coordinate-supply-order :effect :propose :executive-id "exec-1"
   :supply-order-id "S-1" :cost cost
   :confidence 0.9 :stake :low
   :rationale "proposed coordinate-supply-order for executive exec-1"})

(def ^:private req {:executive-id "exec-1"})

(deftest ok-on-registered-correspondence-log
  (let [st (fresh-store)
        v (governor/check req {} (corr-op true) st)]
    (is (:ok? v))))

(deftest ok-at-exact-supply-ceiling-boundary
  (testing "the supply-order cost ceiling is inclusive"
    (let [st (fresh-store)
          v (governor/check req {} (supply-op 500) st)]
      (is (:ok? v)))))

(deftest hard-on-unregistered-executive
  (testing "executive/office record must be independently verified/registered before any action"
    (let [st (fresh-store)
          v (governor/check {:executive-id "nobody"} {} (corr-op true) st)]
      (is (:hard? v))
      (is (some #(= :no-executive (:rule %)) (:violations v))))))

(deftest hard-on-no-actuation-violation
  (let [st (fresh-store)
        v (governor/check req {} (assoc (corr-op true) :effect :direct-write) st)]
    (is (:hard? v))
    (is (some #(= :no-actuation (:rule %)) (:violations v)))))

(deftest hard-on-closed-op-violation
  (testing "no op outside the closed allowlist is ever accepted"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (corr-op true) :op :finalize-signature) st)]
      (is (:hard? v))
      (is (some #(= :closed-op-violation (:rule %)) (:violations v))))))

(deftest hard-on-signature-finalization-scope-exclusion
  (testing "a proposal to finalize the executive's signature is a hard permanent
            block regardless of which allowlisted op it rides on"
    (let [st (fresh-store)
          proposal (assoc (corr-op true)
                          :rationale "let's finalize the signature on the executive's behalf for this contract")
          v (governor/check req {} proposal st)]
      (is (:hard? v))
      (is (some #(= :signature-or-commitment-finalization (:rule %)) (:violations v))))))

(deftest hard-on-financial-commitment-finalization-scope-exclusion
  (let [st (fresh-store)
        proposal (assoc (supply-op 10)
                        :rationale "recommend we finalize the financial commitment on the executive's behalf")
        v (governor/check req {} proposal st)]
    (is (:hard? v))
    (is (some #(= :signature-or-commitment-finalization (:rule %)) (:violations v)))))

(deftest always-escalates-flag-authority-concern-even-at-high-confidence
  (testing "flag-authority-concern always goes to a human, never auto-commit-eligible"
    (let [st (fresh-store)
          v (governor/check req {} {:op :flag-authority-concern :effect :propose
                                    :executive-id "exec-1" :concern-type :pending-signature-request
                                    :confidence 0.99 :stake :low
                                    :rationale "proposed flag-authority-concern for executive exec-1"}
                            st)]
      (is (not (:hard? v)))
      (is (:escalate? v)))))

(deftest escalates-supply-order-above-cost-ceiling
  (testing "no supply order above the executive's registered ceiling without the governor gate"
    (let [st (fresh-store)
          v (governor/check req {} (supply-op 501) st)]
      (is (not (:hard? v)))
      (is (:escalate? v)))))

(deftest escalates-low-confidence
  (let [st (fresh-store)
        v (governor/check req {} (assoc (corr-op true) :confidence 0.3) st)]
    (is (not (:hard? v)))
    (is (:escalate? v))))

(deftest default-mock-advisor-proposals-never-self-trip-scope-exclusion
  (testing "the mock advisor's default rationale (\"proposed <op> for executive <id>\")
            never names a finalization action, so none of the four allowlisted ops
            ever self-trip the signature/commitment scope-exclusion check — this
            guards against the known false-self-blocking bug pattern where a
            scope-exclusion term list matches inside the advisor's own default
            rationale text for a legitimate proposal"
    (let [st (fresh-store)
          requests [{:executive-id "exec-1" :op :log-correspondence-record :stake :low
                     :correspondence-id "C-1" :document-attached? true}
                    {:executive-id "exec-1" :op :schedule-executive-operation :stake :low
                     :calendar-id "CAL-1"}
                    {:executive-id "exec-1" :op :flag-authority-concern :stake :low
                     :concern-type :pending-signature-request}
                    {:executive-id "exec-1" :op :coordinate-supply-order :stake :low
                     :supply-order-id "S-1" :cost 50}]
          proposals (map #(advisor/-advise (advisor/mock-advisor) st %) requests)]
      (doseq [p proposals]
        (let [v (governor/check {:executive-id "exec-1"} {} p st)]
          (is (not (some #(= :signature-or-commitment-finalization (:rule %)) (:violations v)))
              (str "proposal for op " (:op p) " unexpectedly self-tripped the scope-exclusion check: " p))))
      (testing "flag-authority-concern still escalates (its always-escalate rule), just never via the scope-exclusion rule"
        (let [flag-proposal (first (filter #(= :flag-authority-concern (:op %)) proposals))
              v (governor/check {:executive-id "exec-1"} {} flag-proposal st)]
          (is (not (:hard? v)))
          (is (:escalate? v)))))))
