# Operator Guide

## First Deployment

1. Define the operator's service area and intake process.
2. Define consent and purpose categories.
3. Run synthetic operating cases.
4. Enable human-reviewed sign-off for `:high`/`:safety-critical` actions.
5. Measure operating outcomes and audit coverage.

## Minimum Production Controls

- consent and disclosure log
- safety-critical escalation path
- provenance for all operating records
- human review for high-risk cases (authority-concern flags, over-ceiling
  supply orders)
- audit export for all gated actions

## Certification

Certified operators must prove that the governor gates every safety-critical
robot action, that authority-concern flags always escalate to humans, and
that no operator or fork ever wires an op that finalizes a signature or
financial/contractual commitment on the executive's behalf into the closed
op-allowlist.
