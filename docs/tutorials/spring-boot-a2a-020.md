# Build a Help Desk Agent with A2A Spring 0.2.0

This tutorial builds and runs a small Spring Boot help-desk agent. It answers
two fixed questions (support hours and password reset) through A2A JSON-RPC.
The example uses only the published A2A Spring 0.2.0 web starter and its
Spring MVC server path.

The [standalone Maven sample](../../samples/tutorial-help-desk) resolves the
released artifacts from Maven Central; it does not depend on a locally
installed snapshot. See its [application class](../../samples/tutorial-help-desk/src/main/java/io/a2aspring/tutorial/HelpDeskApplication.java),
[agent implementation](../../samples/tutorial-help-desk/src/main/java/io/a2aspring/tutorial/HelpDeskAgent.java),
and [configuration](../../samples/tutorial-help-desk/src/main/resources/application.yml).

## Compatibility

- A2A Spring `0.2.0`
- Official A2A Java SDK `1.5.1.Final` (managed by the starter)
- Java 17+
- Spring Boot `3.5.6`

The release supports Spring MVC, JSON-RPC at `POST /a2a`, Agent Card discovery,
and SSE streaming. The sample uses the SDK's in-memory task infrastructure.
It does not configure authentication, persistent task storage, or outbound
push delivery.

## 1. Inspect the application

The Maven project depends on the released web starter:

```xml
<dependency>
  <groupId>io.github.soheilqalamkari</groupId>
  <artifactId>a2a-spring-boot-starter-web</artifactId>
  <version>0.2.0</version>
</dependency>
```

`HelpDeskApplication` is a regular Spring Boot application. The starter's
auto-configuration discovers `HelpDeskAgent`, which implements the official
SDK's `AgentExecutor` interface. The agent reads the first text part, selects a
short help-desk answer, and emits it as a completed task artifact. The full
implementation, including imports and answer mapping, is in the linked sample
source above. Its core request handling is:

```java
@Component
public class HelpDeskAgent implements AgentExecutor {
    @Override
    public void execute(RequestContext context, AgentEmitter emitter) {
        if (context.getTask() == null) {
            emitter.submit();
        }

        String question = context.getMessage().parts().stream()
                .filter(TextPart.class::isInstance)
                .map(TextPart.class::cast)
                .map(TextPart::text)
                .findFirst()
                .orElse("");

        emitter.startWork();
        emitter.addArtifact(List.of(new TextPart(answerFor(question))));
        emitter.complete();
    }

    @Override
    public void cancel(RequestContext context, AgentEmitter emitter) {
        emitter.cancel();
    }
}
```

The example's answers are deliberately local and deterministic. Replace
`answerFor` with your application's business logic while keeping the SDK
`AgentExecutor` and `AgentEmitter` contract.

The app's `application.yml` configures the Agent Card and selects the supported
JSON-RPC transport. Push notification delivery remains disabled:

```yaml
a2a:
  agent:
    name: help-desk-agent
    description: Answers basic help-desk questions about support hours and password reset
    version: 1.0.0
    url: http://localhost:8080/a2a
  server:
    enabled: true
    transport: JSONRPC
    push-notifications-enabled: false
```

## 2. Run the checks and start the agent

From the repository root:

```bash
cd samples/tutorial-help-desk
mvn --batch-mode --no-transfer-progress verify
mvn spring-boot:run
```

The first command compiles and tests this app against the published `0.2.0`
artifact. The tests check Agent Card discovery and a JSON-RPC request. Keep the
server running for the requests below.

## 3. Discover the Agent Card

In another terminal:

```bash
curl --fail --silent http://localhost:8080/.well-known/agent-card.json
```

The response describes `help-desk-agent` and its supported JSON-RPC interface.
The endpoint is useful to A2A clients that discover agents through their Agent
Cards.

## 4. Send a help-desk question

Send a text message using the A2A `message/send` JSON-RPC method. The version
header is `1.0`, matching the protocol interface advertised by the sample's
Agent Card:

```bash
curl --fail --silent --show-error \
  -X POST http://localhost:8080/a2a \
  -H 'Content-Type: application/json' \
  -H 'A2A-Version: 1.0' \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "message/send",
    "params": {
      "message": {
        "messageId": "question-1",
        "contextId": "help-desk-session",
        "role": "ROLE_USER",
        "parts": [
          {"kind": "text", "text": "What are your support hours?"}
        ]
      }
    }
  }'
```

The JSON-RPC result contains a completed task and a text artifact with:

```text
The help desk is open Monday to Friday, 09:00–17:00 UTC.
```

Try `How do I reset my password?` to exercise the other answer. Unknown
questions receive a short prompt describing the topics this sample handles.

## 5. Request the streaming method

The same executor can be called through A2A's `message/stream` method. `curl
-N` leaves the response stream open so you can see the server-sent events:

```bash
curl --no-buffer --fail --silent --show-error \
  -X POST http://localhost:8080/a2a \
  -H 'Content-Type: application/json' \
  -H 'A2A-Version: 1.0' \
  -H 'Accept: text/event-stream' \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "message/stream",
    "params": {
      "message": {
        "messageId": "question-2",
        "contextId": "help-desk-session",
        "role": "ROLE_USER",
        "parts": [
          {"kind": "text", "text": "How do I reset my password?"}
        ]
      }
    }
  }'
```

The response is SSE (`text/event-stream`) and includes the task lifecycle and
answer events. A production client should use an A2A client library to parse
these events and handle reconnects.

## What this release does not provide

This sample is a tutorial, not a production help-desk system. Its answers are
hard-coded, default task storage is in memory, and it has no authentication or
outbound push-notification sender. A2A Spring 0.2.0 has partial TCK evidence;
it does not claim full protocol conformance. See the
[verified TCK results](../tck.md) and
[compatibility notes](../compatibility.md) before choosing capabilities for
your deployment.
