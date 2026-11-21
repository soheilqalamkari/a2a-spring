# Metrics and observability

The optional `a2a-spring-boot-starter-observability` module adds Micrometer
metrics for the Spring MVC JSON-RPC endpoint. It is not included by
`a2a-spring-boot-starter-web`; applications that do not add the optional
starter have no A2A-specific Micrometer instrumentation.

## Dependency

For the 0.3.0 development line, add the matching snapshot version:

```xml
<dependency>
  <groupId>io.github.soheilqalamkari</groupId>
  <artifactId>a2a-spring-boot-starter-observability</artifactId>
  <version>0.3.0-SNAPSHOT</version>
</dependency>
```

The starter brings Spring Boot Actuator and Micrometer. A `MeterRegistry` bean
is required; Spring Boot's registry auto-configuration supplies one for
supported registries. Applications can replace the default
`A2AWebMetricsRecorder` bean to customize recording.

Expose the Actuator endpoints required by your deployment. For example:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics
```

Protect management endpoints using the application's normal Spring Security
and network policies. Exporter-specific setup, such as Prometheus or OTLP,
uses the standard Micrometer and Spring Boot configuration for that registry.

## Meter reference

All durations are recorded in seconds. Counters and timers use only bounded
tag values.

| Meter | Type and unit | Tags | Meaning |
| --- | --- | --- | --- |
| `a2a.server.requests` | Counter | `method`, `outcome` | Completed JSON-RPC endpoint requests |
| `a2a.server.request.duration` | Timer, seconds | `method`, `outcome` | Time spent dispatching and preparing each HTTP response |
| `a2a.server.task.outcomes` | Counter | `outcome` | Terminal task states observed in JSON-RPC responses and SSE events |
| `a2a.server.streams.active` | Gauge, streams | none | SSE streams not yet completed, timed out, or errored |
| `a2a.server.stream.duration` | Timer, seconds | `outcome` | Lifetime of a server-sent event stream |

`method` is limited to the supported A2A methods (`message/send`,
`message/stream`, `tasks/get`, `tasks/list`, `tasks/cancel`,
`tasks/resubscribe`, the four push-notification configuration methods, and
`agent/card`). Unknown values are recorded as `other`.

Request `outcome` is one of `success`, `streaming`, `unsupported_content_type`,
`parse_error`, `invalid_request`, `protocol_error`, `invalid_params`, `error`,
or `other`. Stream `outcome` is `success`, `timeout`, `error`, or `other`.
Task `outcome` is one of the terminal SDK states `completed`, `failed`,
`canceled`, `rejected`, or `other`.

No metric tags contain task IDs, context IDs, message IDs, user names,
request headers, or arbitrary Agent Card values. The task outcome counter
counts terminal state observations in returned task representations; repeated
responses for the same task can therefore produce additional observations.

These meters describe the Spring MVC JSON-RPC adapter. They do not claim to
cover work performed after a response or stream has completed, nor do they
instrument transports this project does not support.
