# Compatibility

The current preview baseline is:

| A2A Spring | Spring Boot | Java | Official A2A Java SDK |
| --- | --- | --- | --- |
| `0.1.x` | `3.5.x` | 17+ | `1.4.0.Final` |

The project currently targets Spring MVC with JSON-RPC and SSE. Java client
interoperability tests are under `a2a-spring-boot-starter-web/src/test`. The
`AbstractA2AServerTest` integration scaffold is present there but disabled at
the current baseline; enabling and passing it is a conformance milestone. See
the latest successful CI run before treating checks as release evidence. See the
[release roadmap](release-roadmap.md) for the release gates and transport
expansion policy.

This is a community-maintained Spring integration of the official SDK. The
SDK remains the protocol implementation. Repository hosting and upstream
listing status are distinct; check the upstream integration list before
describing the project as listed or organization-maintained.
