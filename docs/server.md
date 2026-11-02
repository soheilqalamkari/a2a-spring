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

WebFlux, HTTP+JSON, gRPC, Spring Security, persistent storage, and observability
are outside the supported 0.1.x scope. Push delivery can be enabled by an
application-supplied sender; the starter itself has no outbound HTTP
implementation.
