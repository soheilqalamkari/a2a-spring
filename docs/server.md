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

WebFlux, HTTP+JSON, gRPC, Spring Security, persistent storage, and observability
are outside the currently supported scope. Push delivery can be enabled by an
application-supplied sender; the starter itself has no outbound HTTP
implementation.
