# Governance

`cloud-itonami-isco-3343` is an OSS open-occupation blueprint. Governance covers
both code and the operator model.

## Maintainers

Maintainers may merge changes that preserve these invariants:

- the Advisor cannot directly dispatch robot actions, disclose records, or
  finalize a signature or financial/contractual commitment on the executive's
  behalf.
- Executive Support Governor remains independent of the advisor.
- hard policy violations cannot be overridden by human approval.
- `:flag-authority-concern` always escalates and is never added to an
  auto-commit set.
- every commit, hold and approval path is auditable.
- real executive/operator data stays outside Git.

## Decision Records

Architecture decisions live in `docs/adr/`. Changes to the trust model,
storage contract, public business model, operator certification or license
should add or update an ADR.

## Operator Governance

Anyone may fork and operate independently. itonami.cloud certification is a
separate trust mark and should require security, audit, support and data-flow
review.

Certified operators can lose certification for:

- bypassing policy checks
- mishandling executive/operator data
- misrepresenting certification status
- failing to respond to security incidents
- hiding material changes to customer-facing operation
- wiring any signature/commitment-finalization op into the closed allowlist
