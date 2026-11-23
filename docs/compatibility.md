# Compatibility

The published 0.2.0 conformance release uses this compatibility baseline:

| A2A Spring | Spring Boot | Java | Official A2A Java SDK |
| --- | --- | --- | --- |
| `0.2.0` (released) | `3.5.x` | 17+ | `1.5.1.Final` |

## Runtime security: CVE-2026-114938 (Java4Shell)

CVE-2026-114938 affects the native Base64 decoder in vulnerable HotSpot JDK builds. Exploitation requires an x86-64 CPU with AVX-512 VBMI support and attacker-controlled Base64 data to reach the affected decoder. The Spring starter does not bundle a JDK, so updating this Maven dependency alone cannot patch the vulnerability. The production Java runtime must be updated separately.

Use a fixed build published by your Java vendor and restart every running JVM. The reported fixed release levels are Java 17.0.21.1, 21.0.13.1, 25.0.5.1, and 27.0.1.1; vendor packaging and build suffixes differ, so confirm the exact fixed build in your vendor's advisory. For example, see the [OpenJDK November 2026 advisory](https://openjdk.org/groups/vulnerability/advisories/2026-11-17), [Oracle's November 2026 CPU](https://www.oracle.com/security-alerts/cspunov2026.html), and [Eclipse Temurin's fixed-build guidance](https://adoptium.net/es/news/2026/11/temurin-november-2026-cspu-java4shell).

If an update cannot be deployed immediately, the vendor-described temporary workaround is to start the JVM with `-XX:-UseBASE64Intrinsics`. Confirm the flag and any required syntax with your runtime vendor, restart the process, and remove the workaround after installing a fixed runtime. This workaround does not replace patching.

This is a runtime issue, not a defect fixed by changing the A2A Spring artifact. The `0.2.0` release remains usable on a Java runtime containing the vendor's CVE fix. The Java 17+ compatibility baseline describes language/runtime compatibility; it does not imply that every JDK update is secure.

The supported 0.2.0 server path is Spring MVC with JSON-RPC and SSE, plus
Agent Card discovery. The starter rejects transport settings other than
`JSONRPC`. Java client interoperability tests are under
`a2a-spring-boot-starter-web/src/test`.

Operational guidance for HTTPS, graceful shutdown, SSE lifetime, readiness,
and deployment boundaries is in [Server behavior](server.md#https-and-production-deployments)
and [Graceful shutdown and deployment operations](server.md#graceful-shutdown-and-deployment-operations).

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
