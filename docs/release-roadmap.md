# A2A Spring Release Roadmap

This roadmap defines release gates for the Spring community integration. It
uses outcomes rather than calendar dates so releases can be tied to verified
readiness and maintainer capacity.

## Positioning

`a2a-spring` integrates the official A2A Java SDK with Spring Boot conventions:
auto-configuration, typed properties, dependency injection, and Spring web
adapters. Protocol behavior remains in the official SDK. Quarkus currently
provides reference implementations for JSON-RPC, HTTP+JSON, and gRPC, while
the Jakarta EE integration targets Jakarta EE Web Profile runtimes. Spring
will first make one transport reliable and idiomatic, then expand based on
community demand.

The upstream integration guide asks for usage instructions and ideally a
sample, tests extending `AbstractA2AServerTest`, visible TCK results, and
Maven Central publication where possible. See the
[official integration guide](https://github.com/a2aproject/a2a-java/blob/main/CONTRIBUTING_INTEGRATIONS.md).

## Release milestones

### 0.1.x — Community preview

The current line is an early preview, not a stable release.

- Supported baseline: Java 17+, Spring Boot 3.5.x, and A2A Java SDK
  1.4.0.Final.
- Supported server path: Spring MVC, JSON-RPC, Agent Card discovery, and SSE
  streaming.
- Keep the runnable sample, starter instructions, compatibility matrix, and
  known limitations current.
- Advertise only capabilities whose behavior is implemented. Push
  notifications remain disabled unless an application supplies a real sender.
- Verify the current upstream integration-list and PR status before making
  claims about the listing or official project status.

### 0.2.0 — Conformance milestone

- `A2ASpringAbstractServerTest` is enabled and passes all 54 tests against a
  real Spring Boot server on the current development branch.
- Complete the applicable JSON-RPC contract: task lifecycle and history,
  terminal states, errors, streaming order and closure, resubscription, and
  concurrent updates.
- Ensure push-notification capability declarations match tested behavior.
  Either test working delivery or leave the capability disabled and document
  it as unsupported.
- The MUST-level JSON-RPC TCK workflow passed and its JSON, HTML, and JUnit
  reports are downloadable. The latest report shows 67 MUST requirements
  passing, 25 skipped, and 22 not tested (75.3% MUST compatibility). Keep the
  SDK, Java, Spring Boot, and TCK versions and revision with each report.
- Track remaining skipped and not-tested requirements explicitly; a successful
  workflow exit does not mean every TCK requirement passed.
- The Spring adapter preserves the request's `A2A-Version` in
  `ServerCallContext`, including absent and explicitly empty values. Integration
  tests verify the pinned SDK accepts the advertised `1.0` interface and
  returns `VersionNotSupportedError` for incompatible `2.0` and defaulted
  `0.3` requests. The SDK 1.4.0.Final validator compares major versions;
  retain that limitation in conformance claims and track exact minor-version
  semantics with the SDK maintainers before marking `VER-SERVER-001` fully
  resolved.

The remaining untested MUST entries are assigned by applicability, rather
than treated as one implementation backlog:

- `VER-CLIENT-001/002` are client requirements owned by the official Java
  client, not this server integration. `VER-SERVER-001` remains a 0.2.0
  server-side verification item.
- `AUTH-TLS-001` is a production deployment requirement; document TLS at the
  deployment boundary. The 0.3.0 optional security module will test
  `AUTH-SERVER-002`, `AUTH-INTASK-001`–`004`, and `AUTH-SCOPE-001`–`003` when
  an application declares authentication requirements or exercises
  authorization-required tasks. These are conditional and are not a claim
  that the unauthenticated sample has passed them.
- `CARD-SIGN-001`–`004` apply only if Agent Card signing is implemented. No
  signing capability is advertised or promised on the current roadmap; add
  it as a separately scoped feature with its own tests before advertising
  signed cards.
- `BIND-EQUIV-001`–`004`, `HTTP_JSON-SVC-001`, and `GRPC-SVC-003` apply only
  when the integration supports those bindings. They remain outside 1.0;
  evaluate them with each transport proposal after 1.0.

### 0.3.0 — Spring production foundations

- Add optional Spring Security integration using Spring Security and official
  SDK authorization extension points.
- Add optional Micrometer/Actuator metrics for requests, task outcomes, and
  streaming; document names and cardinality expectations.
- Add a persistent task-store adapter only after documenting its lifecycle
  and compatibility contract. Keep in-memory storage as the default.
- Document authentication, storage replacement, graceful shutdown, and
  supported deployment configurations, including production TLS termination
  and the conditional authentication/authorization test scope above.

### 1.0.0 — Stable Spring MVC release

- Require the conformance milestone, stable public APIs, compatibility CI,
  complete sample and setup documentation, and a support and upgrade policy.
- Publish signed source and Javadoc artifacts to Maven Central and verify
  resolution in a clean consumer project. If Central publication is blocked,
  provide reproducible build and local-install instructions.
- Keep WebFlux, HTTP+JSON, gRPC, and Spring AI outside the 1.0 release gate.

### Post-1.0 — Expand with demand and upstream alignment

- Add WebFlux as a separate adapter/starter with independent streaming and
  conformance coverage.
- Evaluate HTTP+JSON and gRPC independently; claim transport support only
  after implementation and evidence exist for each.
- Keep Spring AI optional and separate from the core protocol integration.
- Track SDK and protocol evolution through compatibility tests and SDK
  upgrades. Near-term upstream topics include task timelines, backward
  compatible event filtering, messages received during active work, and
  bidirectional streaming. See the
  [A2A protocol roadmap](https://github.com/a2aproject/A2A/blob/main/docs/roadmap.md).

## Release gates and ownership

Every release candidate must pass `mvn verify`, the enabled official server
test contract, and the CI TCK job for the transports and capabilities it
advertises. Update the compatibility matrix, changelog, sample, dependency
coordinates, known limitations, and TCK report link for each release.

The official A2A Java SDK owns protocol behavior. This project owns Spring
auto-configuration, properties, bean replacement, transport adaptation, and
Spring ecosystem integrations.

## Current baseline

The repository remains on `0.1.0-SNAPSHOT`. The official abstract server test
contract passes, and the MUST-level JSON-RPC TCK workflow has produced a
downloadable report. The TCK evidence remains partial because some checks are
skipped or not tested. Maven Central publication and a clean-consumer
resolution check remain release work.
