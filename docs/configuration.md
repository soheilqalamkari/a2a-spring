# Configuration

The root property is `a2a`.

| Property | Default | Meaning |
| --- | --- | --- |
| `a2a.agent.name` | `a2a-agent` | Agent Card name |
| `a2a.agent.description` | `A2A agent` | Agent Card description |
| `a2a.agent.version` | `0.1.0` | Agent Card version |
| `a2a.agent.url` | `http://localhost:8080/a2a` | JSON-RPC endpoint advertised in the card |
| `a2a.server.enabled` | `true` | Enables the A2A runtime when an `AgentExecutor` exists |
| `a2a.server.transport` | `JSONRPC` | Transport selected by the web starter |
| `a2a.security.enabled` | `false` | Reserved for the Spring Security module |

Spring's normal property sources are supported, including YAML,
`application.properties`, environment variables, command-line arguments, and
external configuration providers.

For example, `A2A_AGENT_NAME=research-agent` can be used by Spring's relaxed
binding rules.
