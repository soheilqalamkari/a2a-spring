# Changelog

## 0.2.0-SNAPSHOT — Conformance candidate

This candidate advances the project from the 0.1.x community preview toward
its 0.2.0 conformance milestone.

- Targets Java 17+, Spring Boot 3.5.x, and A2A Java SDK 1.5.1.Final.
- Supports Spring MVC with JSON-RPC, Agent Card discovery, and SSE.
- Passes all 54 cases in the official `AbstractA2AServerTest` contract.
- Includes the merged A2A Java SDK version-negotiation fix from
  [issue #1294](https://github.com/a2aproject/a2a-java/issues/1294), with
  focused Spring server tests for supported, unsupported, patch, absent, and
  empty version values.
- Records partial JSON-RPC TCK evidence: 66 MUST passed, 25 skipped, 17 not
  tested, and 7 pending manual verification. A separate scoped run passes the
  required-extension JSON-RPC check. This is not full TCK conformance.
- Push notifications remain disabled unless an application supplies a sender
  and enables delivery.

This is a snapshot candidate, not a published release. See
[`docs/tck.md`](docs/tck.md) for exact report links, versions, and remaining
coverage classifications, and [`docs/release-roadmap.md`](docs/release-roadmap.md)
for milestone gates.
