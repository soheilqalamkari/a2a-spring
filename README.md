# A2A Spring Boot

Spring-native integration for the [official A2A Java SDK](https://github.com/a2aproject/a2a-java).

This project provides the foundation for running an A2A agent in a Spring Boot application. It keeps protocol behavior in the official SDK and focuses on Spring Boot conventions: auto-configuration, typed properties, dependency injection, and transport adapters.

## Current status

This is an early MVP foundation. It currently provides:

- `a2a-spring-core` with typed properties and Spring `Environment` configuration;
- `a2a-spring-boot-autoconfigure` with agent-card and executor discovery;
- `a2a-spring-boot-starter` for application dependencies;
- `a2a-spring-boot-starter-web` with Spring MVC JSON-RPC hosting;
- a runnable `samples/hello-world` application.

The web starter exposes `GET /.well-known/agent-card.json` and `POST /a2a` for JSON-RPC requests, including `message/send`, task operations, push-configuration operations, and SSE-based `message/stream`. Official-client interoperability tests pass, and the advertised JSON-RPC interface passes the applicable TCK checks; see [TCK conformance](docs/tck.md) for scope and remaining upstream requirements.

Documentation:

- [Getting started](docs/getting-started.md)
- [Configuration](docs/configuration.md)
- [Server behavior](docs/server.md)
- [Compatibility](docs/compatibility.md)
- [TCK conformance](docs/tck.md)
- [Release process](docs/releasing.md)
- [Maintainer proposal](docs/maintainer-proposal.md)
- [Upstream submission checklist](docs/upstream-submission.md)

## Quick start

```xml
<dependency>
  <groupId>io.github.soheilghalamkari</groupId>
  <artifactId>a2a-spring-boot-starter-web</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Register an official SDK `AgentExecutor` as a Spring bean:

```java
@Component
class MyAgent implements AgentExecutor {
    @Override
    public void execute(RequestContext context, AgentEmitter emitter) {
        if (context.getTask() == null) emitter.submit();
        emitter.startWork();
        emitter.addArtifact(List.of(new TextPart("Hello from Spring")));
        emitter.complete();
    }
}
```

Configure it with standard Spring configuration:

```yaml
a2a:
  agent:
    name: weather-agent
    description: Provides weather information
    version: 1.0.0
    url: http://localhost:8080/a2a
  server:
    enabled: true
    transport: JSONRPC
```

## Build

Requires Java 17+ and Maven 3.9+.

```bash
mvn verify
```

## Project direction

The planned modules are documented in the project requirements. The next increments are WebFlux, security, observability, persistence, push notification delivery, and official TCK coverage as separate, focused modules.

## Contributing and publication

See [CONTRIBUTING.md](CONTRIBUTING.md). This is an independent project prepared
for proposal to the A2A maintainers. Publication to the official A2A GitHub
organization and A2A documentation site requires maintainer review and an
accepted upstream pull request; this repository does not claim official status.
