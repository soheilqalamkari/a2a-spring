# Changelog

## 0.2.0 — Conformance

Published to Maven Central on 2026-11-16. This is the first conformance milestone release for the Spring MVC integration.

- Targets Java 17+, Spring Boot 3.5.x, and A2A Java SDK 1.5.1.Final.
- Supports Spring MVC with JSON-RPC, Agent Card discovery, and SSE.
- Passes all 54 cases in the official `AbstractA2AServerTest` contract.
- Includes the merged A2A Java SDK version-negotiation fix from [issue #1294](https://github.com/a2aproject/a2a-java/issues/1294), with focused Spring server tests for supported, unsupported, patch, absent, and empty version values.
- Records partial JSON-RPC TCK evidence: 66 MUST passed, 25 skipped, 17 not tested, and 7 pending manual verification. A separate scoped run passes the required-extension JSON-RPC check. This is not full TCK conformance.
- Push notifications remain disabled unless an application supplies a sender and enables delivery.

Published coordinates are documented in the [Getting started guide](docs/getting-started.md). The release workflow completed successfully in [run #1](https://github.com/soheilqalamkari/a2a-spring/actions/runs/43844344677). See [TCK conformance](docs/tck.md) for exact report links, versions, and remaining coverage classifications, and the [release roadmap](docs/release-roadmap.md) for future milestones.
