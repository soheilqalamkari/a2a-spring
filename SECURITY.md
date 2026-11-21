# Security policy

Do not report security vulnerabilities in public issues. Send a private report
to the repository maintainers with the affected version, reproduction steps,
and any relevant logs or proof of concept.

The `0.3.0-SNAPSHOT` development line introduces an optional Spring Security
adapter in `a2a-spring-boot-starter-security`. It maps authenticated Spring
Security principals to the official SDK user context. It does not install an
HTTP security filter chain, define endpoint access rules, or declare security
requirements in the Agent Card. Applications must add and configure Spring
Security for endpoint authentication and authorization.

Task-level authorization uses the SDK's `TaskAuthorizationProvider`. It is
independent from endpoint authentication. When `a2a.security.enabled=true`,
applications must supply that provider; startup fails if it is absent. See
the [security integration guide](docs/security.md) for setup and boundaries.

This adapter is under development and is not part of the published `0.2.0`
artifacts. Do not treat snapshot APIs as a stable security contract.
