# A2A Spring Boot

A community-maintained Spring Boot integration for the [official A2A Java SDK](https://github.com/a2aproject/a2a-java), now listed in the SDK's [server integrations](https://github.com/a2aproject/a2a-java/blob/main/docs/content/dev/server.md#server-integrations).

This project provides the foundation for running an A2A agent in a Spring Boot application. It keeps protocol behavior in the official SDK and focuses on Spring Boot conventions: auto-configuration, typed properties, dependency injection, and transport adapters.

## Current status

The integration is listed in the A2A Java SDK ecosystem. The project remains an early MVP foundation and currently provides:

- `a2a-spring-core` with typed properties and Spring `Environment` configuration;
- `a2a-spring-boot-autoconfigure` with agent-card and executor discovery;
- `a2a-spring-boot-starter` for application dependencies;
- `a2a-spring-boot-starter-web` with Spring MVC JSON-RPC hosting;
- a runnable `samples/hello-world` application.

The web starter supports Spring MVC with JSON-RPC at `POST /a2a`, Agent
Card discovery at `GET /.well-known/agent-card.json`, and SSE for streaming
methods. The Agent Card advertises only JSON-RPC. Push-notification
configuration APIs are provided by the SDK, but delivery is disabled unless
the application supplies a `PushNotificationSender` and explicitly enables
the feature. The official `AbstractA2AServerTest` passes all 54 cases on the
current development branch; the TCK still needs a successful CI run. Tests
using the official Java client are included; see
[TCK conformance](docs/tck.md) for verified results, scope, and remaining
requirements.

Documentation:

- [Getting started](docs/getting-started.md)
- [Configuration](docs/configuration.md)
- [Server behavior](docs/server.md)
- [Compatibility](docs/compatibility.md)
- [TCK conformance](docs/tck.md)
- [Release roadmap](docs/release-roadmap.md)
- [Release process](docs/releasing.md)
- [Project and upstream status](docs/maintainer-proposal.md)
- [Upstream integration listing](docs/upstream-submission.md)
- [Development workflow](docs/development-workflow.md)

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
    push-notifications-enabled: false
```

The starter uses in-memory task and queue implementations by default. Replace
the SDK `TaskStore`, `TaskStateProvider`, `QueueManager`,
`PushNotificationConfigStore`, or `PushNotificationSender` with Spring beans
when an application needs custom storage or delivery. Push notifications stay
disabled by default. Enabling the property also requires an application-provided
`PushNotificationSender`; startup fails if delivery is enabled without one.

## Build

Requires Java 17+ and Maven 3.9+.

```bash
mvn verify
```

## Project direction

See the [release roadmap](docs/release-roadmap.md) for milestone scope and
release gates. The first stable release targets Spring MVC with JSON-RPC and
SSE; WebFlux, additional transports, and Spring AI are later, separate
increments.

## Project status and relationship to A2A Java

The A2A Java maintainers accepted [PR #1201](https://github.com/a2aproject/a2a-java/pull/1201), adding this project to the SDK's server integration list. This is a community integration developed and maintained in this repository. The listing does not transfer the repository to the A2A organization, make this code part of the `a2a-java` codebase, or imply that the A2A project maintains or endorses this repository.

See [CONTRIBUTING.md](CONTRIBUTING.md) for how to propose changes. Contributions are welcome through pull requests and are reviewed by the repository maintainer.
