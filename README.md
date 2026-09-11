# cloud-itonami-isco-3343

Open Occupation Blueprint for **ISCO-08 3343**: Administrative and Executive Secretaries.

This repository designs a forkable OSS business for an independent executive support practice: a correspondence-intake and scheduling robot manages correspondence records, calendar operations and office-supply drafts under a governor-gated actor, so the practice keeps its own operating records instead of renting a closed executive-assistant SaaS.

**Maturity: `:implemented`.** `src/execsecretary/` implements the
`ExecutiveSupportActor` as a `langgraph.graph/state-graph`
(`execsecretary.actor`) wired to an `Executive Support Advisor`
(`execsecretary.advisor`) and an independent `ExecutiveSupportGovernor`
(`execsecretary.governor`), following the itonami actor pattern
(ADR-2607011000): `:intake -> :advise -> :govern -> :decide -+-> :commit
(:ok?) +-> :request-approval (:escalate?, human-in-the-loop interrupt)
+-> :hold (:hard?)`. 15 tests / 37 assertions green (`kbb -M:test`).
HARD invariants (always hold, never overridable): executive/office
provenance (the executive record must be independently verified/
registered before any action), no-actuation (`:effect` must be
`:propose`), a closed op-allowlist (`:log-correspondence-record`,
`:schedule-executive-operation`, `:flag-authority-concern`,
`:coordinate-supply-order` — no other `:op` is ever accepted), and a
dedicated signature/commitment-finalization scope-exclusion: any
proposal that names finalizing a signature or a financial/contractual
commitment on the executive's behalf is a hard, permanent block,
independent of which allowlisted op it rides on. Always-escalate
(human sign-off regardless of confidence, mapping this repo's Trust
Controls in [`docs/business-model.md`](docs/business-model.md)):
`:flag-authority-concern` (never auto-commit-eligible) and any
`:coordinate-supply-order` above the executive's registered
supply-order cost ceiling.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical/administrative domain work**. Here a correspondence-intake and scheduling robot performs document filing, calendar coordination and supply-order drafting under an actor that proposes
actions and an independent **Executive Support Governor** that gates them. The governor never
dispatches hardware itself and never finalizes a signature or financial/contractual commitment on the executive's behalf; `:high`/`:safety-critical` actions (such as flagging an authority concern, or a supply order above the registered cost ceiling) require human sign-off.

## Core Contract

```text
executive correspondence intake + calendar + office-supply policy
        |
        v
Executive Support Advisor -> Executive Support Governor -> log/schedule/order, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, suppress
an operating record, finalize a signature or commitment on the executive's
behalf, or disclose sensitive data without governor approval and audit
evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `3343`). Required capabilities:

- :robotics
- :identity
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
