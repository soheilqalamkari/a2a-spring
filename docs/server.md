# Server behavior

The web starter currently provides a Spring MVC JSON-RPC adapter backed by the
official A2A Java SDK server request handler.

## Endpoints

- `GET /.well-known/agent-card.json` returns the configured Agent Card.
- `POST /a2a` accepts JSON-RPC `message/send` requests.
- `POST /a2a` also accepts the official Java SDK client's `SendMessage`
  operation name, which is equivalent to `message/send`.

Unsupported methods return a JSON-RPC method-not-found error with code
`-32601`.

The starter uses an in-memory task store and intentionally disables push
notification delivery until an application supplies a real
`PushNotificationSender` bean. Applications can replace the task store,
request handler, event bus, executor, or push sender with Spring beans.

Incoming servlet requests are converted to the SDK's `ServerCallContext` by a
dedicated `ServerCallContextFactory`. The default factory always provides a
non-null SDK `User`: unauthenticated requests use the `anonymous` user, while a
servlet principal is mapped to an authenticated user. Applications can replace
the factory when their authentication or tenant model needs richer context.

Streaming, WebFlux, security, persistence, and observability are planned
modules rather than promises of the current MVP.
