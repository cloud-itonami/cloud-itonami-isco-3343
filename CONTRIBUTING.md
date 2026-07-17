# Contributing

`cloud-itonami-isco-3343` accepts contributions to the OSS actor, policy tests,
documentation, examples and open occupation blueprint.

## Development

```bash
clojure -M:dev:test
clojure -M:lint
```

Keep changes small and include tests for policy, audit, store or disclosure
behavior.

## Rules

- Do not commit real executive/client data, credentials or operating documents.
- Keep production writes and disclosures behind Executive Support Governor.
- Never add an `:op` to the closed allowlist that finalizes a signature or a
  financial/contractual commitment on the executive's behalf — that decision
  is always the executive's own act.
- Treat this occupation's workflows as high-risk: add tests for permission,
  purpose, safety and audit logging.
- Document any new business-model or operator assumption in `docs/`.

## Pull Requests

PRs should describe:

- what behavior changed
- which policy invariant is affected
- how it was tested
- whether operator or certification docs need updates
