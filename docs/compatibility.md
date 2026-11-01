# Compatibility

The current preview baseline is:

| A2A Spring | Spring Boot | Java | Official A2A Java SDK |
| --- | --- | --- | --- |
| `0.1.x` | `3.5.x` | 17+ | `1.4.0.Final` |

The supported 0.1.x server path is Spring MVC with JSON-RPC and SSE, plus
Agent Card discovery. The starter rejects transport settings other than
`JSONRPC`. Java client interoperability tests are under
`a2a-spring-boot-starter-web/src/test`.

`A2ASpringAbstractServerTest` exists but is disabled in 0.1.x. Passing that
contract and demonstrating applicable TCK conformance are 0.2.0 gates, not
claims of this preview. See [TCK conformance](tck.md) and the
[release roadmap](release-roadmap.md) for current scope and evidence policy.

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
- `A2ASpringAbstractServerTest` is disabled, and full TCK conformance is not
  claimed. These are tracked as 0.2.0 conformance work.
