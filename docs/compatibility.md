# Compatibility

The current development baseline is:

| A2A Spring | Spring Boot | Java | Official A2A Java SDK |
| --- | --- | --- | --- |
| `0.1.x` | `3.5.x` | 17+ | `1.4.0.Final` |

The build verifies both Spring MVC requests and a real HTTP request made by
the official A2A Java JSON-RPC client transport. The compatibility test is in
`a2a-spring-boot-starter-web/src/test`.

This project is an independent Spring integration. It should not be described
as an official A2A organization repository until maintainers accept a proposal
or upstream contribution.
