(ns execsecretary.advisor
  "Executive Support Advisor — the advisor named in this repository's
  README, proposing an executive-support operation (log a
  correspondence record, schedule a calendar operation, flag an
  authority concern, coordinate a supply order) from an executive's
  correspondence intake, calendar and office-supply policy. Swappable
  mock/llm; the advisor ONLY proposes — `execsecretary.governor`
  independently checks the closed op allowlist, the supply-order cost
  ceiling and any signature/commitment-finalization scope-exclusion,
  and always escalates `:flag-authority-concern`. Modeled on
  cloud-itonami-isco-3313's accountingsupport.advisor.

  A proposal: {:op :log-correspondence-record|:schedule-executive-operation|
                   :flag-authority-concern|:coordinate-supply-order
               :effect :propose :executive-id str
               :correspondence-id str :calendar-id str
               :supply-order-id str :cost number :concern-type kw
               :stake kw :confidence n :rationale str}

  The `:rationale` this advisor produces is deliberately generic
  (\"proposed <op> for executive <id>\") — it never narrates a
  finalization action, so it structurally cannot self-trip the
  governor's signature/commitment scope-exclusion check (see
  `execsecretary.governor` docstring and
  `execsecretary.governor-test/default-mock-advisor-proposals-never-self-trip-scope-exclusion`)."
  )

(defprotocol Advisor
  (-advise [advisor store request] "request -> proposal map"))

(defn- infer [_store {:keys [op stake executive-id correspondence-id calendar-id
                              supply-order-id cost concern-type document-attached?]
                       :as request}]
  (cond-> {:op op
           :effect :propose
           :executive-id executive-id
           :stake (or stake :low)
           :confidence (case (or stake :low) :high 0.7 :medium 0.85 :low 0.95)
           :rationale (str "proposed " (name op) " for executive " executive-id)}
    correspondence-id     (assoc :correspondence-id correspondence-id)
    calendar-id            (assoc :calendar-id calendar-id)
    supply-order-id        (assoc :supply-order-id supply-order-id)
    (some? cost)           (assoc :cost cost)
    concern-type            (assoc :concern-type concern-type)
    (some? document-attached?) (assoc :document-attached? (boolean document-attached?))))

(defn mock-advisor []
  (reify Advisor
    (-advise [_ store request] (infer store request))))

(def ^:private system-prompt
  "You are an executive-support advisor. Given a request, propose an
   :op, the :executive-id and the op-specific fields (correspondence
   id, calendar id, supply-order id and cost, or authority-concern
   type), an honest :confidence and a :stake. You may only propose
   :log-correspondence-record, :schedule-executive-operation,
   :flag-authority-concern or :coordinate-supply-order — never propose
   finalizing a signature or a financial/contractual commitment on the
   executive's behalf; that is always the executive's own act, never
   this advisor's. If you notice anything that looks like a
   signing-authority or commitment risk, propose
   :flag-authority-concern so a human can review it — do not describe
   yourself as finalizing or executing it. The governor independently
   checks the supply-order cost ceiling and always requires human
   sign-off for :flag-authority-concern regardless of confidence.")

(defn- parse-proposal [content]
  (try
    (let [p (read-string content)]
      (if (map? p)
        (assoc p :effect :propose)
        {:op :unknown :effect :propose :confidence 0.0 :stake :high
         :rationale "unparseable LLM response"}))
    (catch #?(:clj Exception :cljs js/Error) _
      {:op :unknown :effect :propose :confidence 0.0 :stake :high
       :rationale "LLM response parse failure"})))

(defn llm-advisor
  [chat-model model-generate-fn gen-opts]
  (reify Advisor
    (-advise [_ _store request]
      (let [msgs [{:role :system :content system-prompt}
                  {:role :user :content (str "operation request: " (pr-str request))}]
            resp (model-generate-fn chat-model msgs gen-opts)]
        (parse-proposal (:content resp))))))
