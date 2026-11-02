# Compatibility

The current preview baseline is:

| A2A Spring | Spring Boot | Java | Official A2A Java SDK |
| --- | --- | --- | --- |
| `0.1.x` | `3.5.x` | 17+ | `1.4.0.Final` |

The supported 0.1.x server path is Spring MVC with JSON-RPC and SSE, plus
Agent Card discovery. The starter rejects transport settings other than
`JSONRPC`. Java client interoperability tests are under
`a2a-spring-boot-starter-web/src/test`.

`A2ASpringAbstractServerTest` is enabled and passes all 54 tests with zero
failures, errors, or skips on the current development branch. Applicable TCK
conformance has not yet been verified. See [TCK conformance](tck.md) and the
[release roadmap](release-roadmap.md) for current evidence and remaining gates.

This community-maintained integration is listed in the official A2A Java
SDK's server integrations directory. The listing does not make the repository
organization-maintained or transfer ownership. The SDK remains the protocol
implementation.

## 0.1.x limitations

- JSON-RPC is the only supported transport; WebFlux, HTTP+JSON, and gRPC are
  not provided.
- Task data uses the SDK's in-memory defaults unless an application replaces
  the store interfaces. Data does not persist across restarts by default.
- The starter does not provide authentication, authorization configuration,
  or outbound HTTP push delivery.
- The full TCK has not yet been run successfully on this branch; do not claim
  TCK conformance until the workflow report is available and reviewed.
