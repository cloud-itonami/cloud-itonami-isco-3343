(ns execsecretary.governor
  "ExecutiveSupportGovernor — the independent safety/traceability layer
  named in this repository's README/business-model.md, gating every
  correspondence log, calendar operation, authority-concern flag and
  supply-order draft an advisor may propose for an executive. The
  governor never dispatches hardware itself and never finalizes a
  signature or a financial/contractual commitment on the executive's
  behalf — that decision is always the executive's own act. Modeled on
  cloud-itonami-isco-3313's accountingsupport.governor. Task twist: a
  proposed supply-order cost is checked against the executive's
  registered `:max-supply-order-cost`, but exceeding it only escalates
  to human sign-off (office procurement is routine) — it does not
  hard-block the way finalizing a signature or commitment does.

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. executive provenance   — the executive/office record must be
                                independently verified/registered
                                before any action.
    2. no-actuation            — proposal :effect must be :propose
                                (the governor never dispatches hardware
                                and never finalizes anything on the
                                executive's behalf; it only gates what
                                the advisor may log/schedule/flag/
                                order).
    3. closed op-allowlist     — the proposed :op must be one of
                                :log-correspondence-record,
                                :schedule-executive-operation,
                                :flag-authority-concern or
                                :coordinate-supply-order. No op that
                                finalizes a signature or a
                                financial/contractual commitment is
                                ever in this list; any other :op is
                                unconditionally rejected.
    4. signature/commitment finalization scope-exclusion — independent
                                of (3), any proposal whose :rationale
                                names the FINALIZATION ACTION itself
                                (e.g. \"finalize the signature on the
                                executive's behalf\", \"execute the
                                contract on the executive's behalf\") is
                                a hard, permanent block, regardless of
                                which allowlisted :op it was attached
                                to. This is deliberately phrased as the
                                finalize/execute ACTION rather than the
                                bare noun (\"signature\"/\"contract\") —
                                a bare-noun match would false-trip on
                                :flag-authority-concern's own default
                                rationale, which legitimately names the
                                concern (e.g. \"a pending signature
                                request\") without proposing to
                                finalize it. See
                                `execsecretary.advisor` docstring and
                                `execsecretary.governor-test` for the
                                dedicated non-self-trip test.
  ESCALATION invariants (:escalate? true, ALWAYS human sign-off per
  business-model.md's Trust Controls — these are :high/
  :safety-critical regardless of confidence):
    5. :op :flag-authority-concern (surfacing a signing-authority or
                                commitment-risk concern always goes to
                                a human — this op is never
                                auto-commit-eligible).
    6. supply-order cost exceeds the executive's registered
                                `:max-supply-order-cost` (no order
                                above the registered ceiling without
                                the governor gate; unlike invariant 4
                                this does not hard-block — it is
                                routine procurement escalated for
                                sign-off).
    7. low confidence (< `confidence-floor`)."
  (:require [clojure.string :as str]
            [execsecretary.store :as store]))

(def confidence-floor 0.6)

(def ^:private closed-ops
  #{:log-correspondence-record :schedule-executive-operation
    :flag-authority-concern :coordinate-supply-order})

(def ^:private always-escalate-ops #{:flag-authority-concern})

;; Phrased as the finalization/execution ACTION, not the bare noun —
;; see invariant 4 above and `execsecretary.governor-test`'s
;; `default-mock-advisor-proposals-never-self-trip-scope-exclusion`.
(def ^:private scope-exclusion-terms
  ["finalize the signature" "finalize a signature" "finalize signature"
   "execute the signature" "execute a signature"
   "sign the contract" "sign on the executive's behalf"
   "signing on the executive's behalf"
   "finalize the contract" "finalize a contract"
   "execute the contract" "commit the executive to a contract"
   "commit the executive financially"
   "authorize the financial commitment" "authorize a financial commitment"
   "finalize the financial commitment" "finalize a financial commitment"
   "finalize the commitment"])

(defn- scope-excluded? [proposal]
  (let [text (str/lower-case (or (:rationale proposal) ""))]
    (boolean (some #(str/includes? text %) scope-exclusion-terms))))

(defn- hard-violations [proposal executive-record]
  (let [{:keys [op]} proposal]
    (cond-> []
      (nil? executive-record)
      (conj {:rule :no-executive :detail "未登録 executive/office"})

      (not= :propose (:effect proposal))
      (conj {:rule :no-actuation
             :detail "effect は :propose のみ許可（governor はいかなる操作も直接実行・確定しない）"})

      (not (contains? closed-ops op))
      (conj {:rule :closed-op-violation
             :detail (str "op " (pr-str op) " は許可された執務秘書業務オペレーション一覧に無い")})

      (scope-excluded? proposal)
      (conj {:rule :signature-or-commitment-finalization
             :detail "executive に代わって署名または財務/契約上のコミットメントを確定する提案は恒久的に禁止（executive 本人の行為のみ）"}))))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a
  `store` implementing `execsecretary.store/Store`. Pure — never
  mutates the store, never finalizes a signature or commitment on the
  executive's behalf."
  [request context proposal store]
  (let [executive-record (store/executive store (:executive-id request))
        hard (hard-violations proposal executive-record)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        always-risky? (contains? always-escalate-ops (:op proposal))
        over-supply-ceiling?
        (and (= :coordinate-supply-order (:op proposal))
             (number? (:cost proposal))
             (some? (:max-supply-order-cost executive-record))
             (> (:cost proposal) (:max-supply-order-cost executive-record)))]
    {:ok? (and (not hard?) (not low?) (not always-risky?) (not over-supply-ceiling?))
     :violations hard
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (or low? always-risky? over-supply-ceiling?))}))
