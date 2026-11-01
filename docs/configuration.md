# Configuration

The root property is `a2a`.

| Property | Default | Meaning |
| --- | --- | --- |
| `a2a.agent.name` | `spring-a2a-agent` | Agent Card name |
| `a2a.agent.description` | `A Spring Boot A2A agent` | Agent Card description |
| `a2a.agent.version` | `0.1.0` | Agent Card version |
| `a2a.agent.url` | `http://localhost:8080/a2a` | JSON-RPC endpoint advertised in the card |
| `a2a.server.enabled` | `true` | Enables the A2A runtime when an `AgentExecutor` exists |
| `a2a.server.transport` | `JSONRPC` | The only transport supported by the Spring MVC starter; other values fail configuration |
| `a2a.server.push-notifications-enabled` | `false` | Enables push delivery only when an application provides a `PushNotificationSender` bean |
| `a2a.security.enabled` | `false` | Reserved for the Spring Security module |

Spring's normal property sources are supported, including YAML,
`application.properties`, environment variables, command-line arguments, and
external configuration providers.

For example, `A2A_AGENT_NAME=research-agent` can be used by Spring's relaxed
binding rules.

When push notifications are enabled without an application-provided sender,
the application fails to start. The default sender is a no-op used only while
the feature is disabled; it is never advertised as delivery support.
