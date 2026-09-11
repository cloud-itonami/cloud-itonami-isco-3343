(ns execsecretary.store
  "SSoT for the ISCO-08 3343 independent executive-support practice
  actor (itonami actor pattern, ADR-2607011000 / CLAUDE.md Actors
  section; README's 'Robotics premise' — a correspondence-intake and
  scheduling robot performs document filing, calendar coordination and
  supply-order drafting under this advisor/governor pair, which never
  dispatches hardware itself and never finalizes a signature or
  financial/contractual commitment on the executive's behalf).
  Modeled on cloud-itonami-isco-3313's accountingsupport.store.

  Domain:

    executive — a registered executive/office record ({:executive-id
                :name :max-supply-order-cost number}). The
                `:max-supply-order-cost` is the registered ceiling a
                proposed supply-order cost is compared against — a
                supply order above it always requires human sign-off
                (it does not hard-block; office supply procurement is
                routine, unlike finalizing a signature or commitment).
    record     — a committed operating record (a posted correspondence
                 log, calendar entry or supply-order draft) — written
                 ONLY via commit-record!.
    ledger     — append-only audit trail, commit or hold."
  )

(defprotocol Store
  (executive [s executive-id])
  (records-of [s executive-id])
  (ledger [s])
  (register-executive! [s exec])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (executive [_ executive-id] (get-in @a [:executives executive-id]))
  (records-of [_ executive-id] (filter #(= executive-id (:executive-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-executive! [s exec]
    (swap! a assoc-in [:executives (:executive-id exec)] exec) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:executives {} :records [] :ledger []}
                                   seed)))))
