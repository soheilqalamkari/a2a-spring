# Server behavior

The web starter currently provides a Spring MVC JSON-RPC adapter backed by the
official A2A Java SDK server request handler.

## Endpoints

- `GET /.well-known/agent-card.json` returns the configured Agent Card.
- `POST /a2a` accepts JSON-RPC message, task, and push-configuration requests.
- `message/stream` and `tasks/resubscribe` return Server-Sent Events with
  `Content-Type: text/event-stream`.
- `POST /a2a` also accepts the official Java SDK client's `SendMessage`
  operation name, which is equivalent to `message/send`.

Unsupported methods return a JSON-RPC method-not-found error with code
`-32601`.

The starter uses the official SDK's in-memory task store, queue manager, and
push-configuration store by default. Each is exposed through the SDK contract
(`TaskStore`, `QueueManager`, `PushNotificationConfigStore`, and related
interfaces), so applications can replace them with Spring beans. A custom
task store must also provide a `TaskStateProvider`.

Push notification configuration and delivery are disabled by default. To
advertise and enable delivery, set
`a2a.server.push-notifications-enabled=true` and provide a real
`PushNotificationSender` bean. The application fails at startup if the
property is enabled without a sender. The starter's safe no-op sender is
available only while the feature is disabled, so it cannot be mistaken for
working delivery. The starter does not provide outbound HTTP delivery;
applications are responsible for configuring that sender.

Incoming servlet requests are converted to the SDK's `ServerCallContext` by a
dedicated `ServerCallContextFactory`. The default factory always provides a
non-null SDK `User`: unauthenticated requests use the `anonymous` user, while a
servlet principal is mapped to an authenticated user. Applications can replace
the factory when their authentication or tenant model needs richer context.

## HTTPS and production deployments

Production A2A traffic must use HTTPS. The starter runs on Spring Boot's web
server, so an application can terminate TLS in the application server or at a
trusted reverse proxy, ingress, or load balancer. The sample's plain HTTP
configuration is for local development only; it is not production TLS
evidence.

For TLS in the Spring Boot application, configure a keystore through a secret
mount or secret manager and keep its password outside source control. For
example:

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: file:/run/secrets/a2a-server.p12
    key-store-type: PKCS12
    key-store-password: ${A2A_TLS_KEYSTORE_PASSWORD}

a2a:
  agent:
    url: https://agents.example.com/a2a
```

When TLS terminates at an edge proxy, configure that proxy to serve HTTPS and
forward requests to the application over a protected network. Set
`a2a.agent.url` to the externally reachable HTTPS endpoint so the Agent Card
advertises the secure URL. Configure trusted forwarded-header handling in the
deployment when the application needs Spring Boot to interpret proxy headers;
do not trust forwarded headers from arbitrary clients.

The TCK cannot establish the TLS configuration of every production deployment.
`AUTH-TLS-001` therefore remains a manual deployment check, not an automated
TCK pass. See [TCK evidence](tck.md) for the exact status and
[release requirements](release-roadmap.md) for the conformance gate.

## Graceful shutdown and deployment operations

Spring Boot 3.5 supports graceful shutdown for the embedded servlet server.
Configure a finite shutdown phase timeout so in-flight HTTP requests have a
bounded opportunity to finish while the server stops accepting new requests:

```yaml
server:
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 20s
```

Spring Boot 3.5 enables graceful shutdown by default, but setting
`server.shutdown: graceful` explicitly makes the deployment intent clear. The
timeout applies to a Spring lifecycle phase; it is not a promise that every
request or stream will finish. Align the platform's termination grace period
to exceed the application timeout and leave time for process cleanup. Send a
normal termination signal such as `SIGTERM`; abrupt termination and IDE stop
buttons may bypass the graceful path. See the Spring Boot 3.5
[graceful-shutdown reference](https://docs.spring.io/spring-boot/3.5/reference/web/graceful-shutdown.html).

During shutdown, the embedded server stops accepting new requests and allows
existing requests time to complete. A long-lived SSE response may therefore
remain open until it ends or the configured shutdown timeout expires. Ending
an SSE response is a transport lifecycle event; it does not cancel the A2A
task. Stream-subscription cleanup is tracked separately in
[issue #52](https://github.com/soheilqalamkari/a2a-spring/issues/52) and its
pull request, and becomes available when that change is included in the
application's version. A client can reconnect using task resubscription while
the task is still available. With the default in-memory task store, process
termination loses task state, and a task that was active before shutdown does
not resume after restart. Persisting task records alone would not persist
execution, queues, events, or live streams.

For deployments behind an orchestrator or load balancer, stop routing new
traffic before terminating the process when the platform supports that
sequence. Configure readiness and health checks in the application or
deployment. Actuator is not a dependency of the base web starter; add and
configure it separately if its endpoints are needed. Keep TLS at the
application server or a trusted edge as described above, and protect the
application-to-edge hop.

WebFlux, HTTP+JSON, gRPC, Spring Security, persistent storage, and observability
are outside the currently supported scope. Push delivery can be enabled by an
application-supplied sender; the starter itself has no outbound HTTP
implementation.
