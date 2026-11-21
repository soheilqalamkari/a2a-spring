# Spring Security integration

The optional `a2a-spring-boot-starter-security` module connects an
application's Spring Security identity to the official A2A Java SDK's
`ServerCallContext`. This module is part of the `0.3.0-SNAPSHOT` development
line and is not included in the published `0.2.0` release.

## Dependency

Add the optional adapter alongside the web starter:

```xml
<dependency>
  <groupId>io.github.soheilqalamkari</groupId>
  <artifactId>a2a-spring-boot-starter-web</artifactId>
  <version>0.3.0-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>io.github.soheilqalamkari</groupId>
  <artifactId>a2a-spring-boot-starter-security</artifactId>
  <version>0.3.0-SNAPSHOT</version>
</dependency>
```

The snapshot is not published to Maven Central. Build and install the project
from its repository with `mvn --batch-mode --no-transfer-progress install`.
Applications must also add the Spring Security components they use, such as
`spring-boot-starter-security`, and configure them using Spring Security's
normal mechanisms.

## Minimal endpoint configuration

The application defines its own endpoint policy. For example, this Spring
Security configuration allows public Agent Card discovery and requires HTTP
Basic authentication for the JSON-RPC endpoint:

```java
@Bean
SecurityFilterChain a2aSecurityFilterChain(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/.well-known/agent-card.json").permitAll()
                    .requestMatchers("/a2a").authenticated()
                    .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults())
            .build();
}
```

This is an example policy, not a default installed by this module. If the
application uses it, its Agent Card must declare matching HTTP Basic security
metadata and requirements. The security adapter does not infer or add that
metadata.

## Identity mapping

For an authenticated Spring Security principal, the default mapper creates an
SDK `AuthenticatedUser` with the principal name. Anonymous or unauthenticated
principals become the SDK `UnauthenticatedUser`. Credentials and authorities
are not copied into the SDK context.

Applications can replace the mapper when the SDK user needs additional,
explicitly selected identity attributes:

```java
@Bean
SpringSecurityUserMapper a2aUserMapper() {
    return authentication -> new AuthenticatedUser(
            authentication.getName(),
            Map.of("tenant", resolveTenant(authentication)));
}
```

Use the `AuthenticatedUser` constructor available in the pinned SDK version
when adding attributes. Avoid placing credentials, tokens, or unnecessary
personal data in the A2A context. Applications can also replace
`ServerCallContextUserResolver` or `ServerCallContextFactory` for a different
identity model.

## Endpoint access and task authorization

The adapter does not create a `SecurityFilterChain`, authenticate HTTP
requests, or choose which endpoints users may call. Those endpoint rules remain
the application's responsibility. The adapter only maps the identity that
Spring Security has already placed on the servlet request.

SDK task authorization is a separate layer. The SDK invokes an application
`TaskAuthorizationProvider` whenever one is supplied. The property below
controls the SDK's `authorizationRequired` behavior and requires a provider
when true:

```yaml
a2a:
  security:
    enabled: true
```

The application fails at startup if this property is true and no provider is
available. Setting it to false does not disable an application's Spring
Security endpoint rules. The provider must implement the application's task
ownership and access policies; the starter cannot infer those policies.

The Agent Card's security schemes and requirements are also application-owned.
Declare them only when the deployed endpoint enforces the corresponding
authentication scheme. Installing this adapter does not modify Agent Card
security metadata.

## Scope

The integration targets the servlet-based Spring MVC JSON-RPC server. It does
not provide a security configuration, token validation, OAuth client/resource
server setup, authorization policy, or WebFlux adapter. The API is a snapshot
and may change before the 0.3.0 release. See the
[release roadmap](release-roadmap.md) and [security policy](../SECURITY.md).
