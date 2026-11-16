# Compatibility

The published 0.2.0 conformance release uses this compatibility baseline:

| A2A Spring | Spring Boot | Java | Official A2A Java SDK |
| --- | --- | --- | --- |
| `0.2.0` (released) | `3.5.x` | 17+ | `1.5.1.Final` |

The supported 0.2.0 server path is Spring MVC with JSON-RPC and SSE, plus
Agent Card discovery. The starter rejects transport settings other than
`JSONRPC`. Java client interoperability tests are under
`a2a-spring-boot-starter-web/src/test`.

`A2ASpringAbstractServerTest` passes all 54 tests with zero failures, errors,
or skips. The latest MUST-level JSON-RPC TCK workflow passed with 66 MUST
requirements passing, 25 skipped, 17 not tested, and 7 pending manual
verification. This is partial TCK evidence, not full conformance. See
[TCK conformance](tck.md) and the
[release roadmap](release-roadmap.md) for the reports and remaining gaps.

This community-maintained integration is listed in the official A2A Java
SDK's server integrations directory. The listing does not make the repository
organization-maintained or transfer ownership. The SDK remains the protocol
implementation.

## Current limitations

- JSON-RPC is the only supported transport; WebFlux, HTTP+JSON, and gRPC are
  not provided.
- Task data uses the SDK's in-memory defaults unless an application replaces
  the store interfaces. Data does not persist across restarts by default.
- The starter does not provide authentication, authorization configuration,
  or outbound HTTP push delivery.
- TCK skips and not-tested requirements remain; see the linked report before
  making claims about any individual capability.
