# Getting started

This project adapts the official A2A Java SDK to Spring Boot. It does not
reimplement the A2A protocol.

## 1. Add the starter

```xml
<dependency>
  <groupId>io.github.soheilghalamkari</groupId>
  <artifactId>a2a-spring-boot-starter-web</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## 2. Register an executor

Any Spring bean implementing the official SDK's `AgentExecutor` interface is
discovered automatically:

```java
@Component
final class HelloAgent implements AgentExecutor {
    @Override
    public void execute(RequestContext context, AgentEmitter emitter) {
        if (context.getTask() == null) {
            emitter.submit();
        }
        emitter.startWork();
        emitter.addArtifact(List.of(new TextPart("Hello from Spring")));
        emitter.complete();
    }
}
```

## 3. Configure the agent

```yaml
a2a:
  agent:
    name: hello-agent
    description: A Spring Boot A2A agent
    version: 0.1.0
    url: http://localhost:8080/a2a
  server:
    enabled: true
    transport: JSONRPC
```

The starter exposes the Agent Card at `/.well-known/agent-card.json` and the
JSON-RPC endpoint at `/a2a`.

Run the sample with:

```bash
mvn -pl samples/hello-world spring-boot:run
```
