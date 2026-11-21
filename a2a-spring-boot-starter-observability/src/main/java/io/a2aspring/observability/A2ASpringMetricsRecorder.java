package io.a2aspring.observability;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import io.a2aspring.web.A2AWebMetricsRecorder;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/** Records bounded-cardinality metrics for the Spring MVC adapter. */
public final class A2ASpringMetricsRecorder implements A2AWebMetricsRecorder {
    private static final Set<String> METHODS = Set.of(
            "message/send", "message/stream", "tasks/get", "tasks/list", "tasks/cancel",
            "tasks/resubscribe", "tasks/pushnotificationconfig/set", "tasks/pushnotificationconfig/get",
            "tasks/pushnotificationconfig/list", "tasks/pushnotificationconfig/delete", "agent/card");
    private static final Set<String> REQUEST_OUTCOMES = Set.of(
            "success", "streaming", "unsupported_content_type", "parse_error", "invalid_request",
            "protocol_error", "invalid_params", "error");
    private static final Set<String> STREAM_OUTCOMES = Set.of("success", "timeout", "error");
    private static final Set<String> TASK_OUTCOMES = Set.of("completed", "failed", "canceled", "rejected");

    private final MeterRegistry registry;
    private final AtomicLong activeStreams = new AtomicLong();

    public A2ASpringMetricsRecorder(MeterRegistry registry) {
        this.registry = registry;
        registry.gauge("a2a.server.streams.active", activeStreams);
    }

    @Override
    public RequestObservation startRequest(String method) {
        String boundedMethod = bounded(method, METHODS);
        Timer.Sample sample = Timer.start(registry);
        return new RequestObservation() {
            private final java.util.concurrent.atomic.AtomicBoolean completed =
                    new java.util.concurrent.atomic.AtomicBoolean();

            @Override
            public void complete(String outcome) {
                if (!completed.compareAndSet(false, true)) {
                    return;
                }
                String boundedOutcome = bounded(outcome, REQUEST_OUTCOMES);
                Counter.builder("a2a.server.requests")
                        .description("Completed A2A JSON-RPC requests")
                        .tag("method", boundedMethod)
                        .tag("outcome", boundedOutcome)
                        .register(registry)
                        .increment();
                sample.stop(Timer.builder("a2a.server.request.duration")
                        .description("Time spent handling an A2A JSON-RPC request")
                        .tag("method", boundedMethod)
                        .tag("outcome", boundedOutcome)
                        .register(registry));
            }

            @Override
            public StreamObservation startStream() {
                activeStreams.incrementAndGet();
                Timer.Sample streamSample = Timer.start(registry);
                return new StreamObservation() {
                    private final java.util.concurrent.atomic.AtomicBoolean completed =
                            new java.util.concurrent.atomic.AtomicBoolean();

                    @Override
                    public void complete(String outcome) {
                        if (completed.compareAndSet(false, true)) {
                            activeStreams.decrementAndGet();
                            streamSample.stop(Timer.builder("a2a.server.stream.duration")
                                    .description("Duration of an A2A server-sent event stream")
                                    .tag("outcome", bounded(outcome, STREAM_OUTCOMES))
                                    .register(registry));
                        }
                    }
                };
            }
        };
    }

    @Override
    public void recordTaskOutcome(String outcome) {
        String boundedOutcome = bounded(outcome, TASK_OUTCOMES);
        Counter.builder("a2a.server.task.outcomes")
                .description("Observed terminal A2A task states")
                .tag("outcome", boundedOutcome)
                .register(registry)
                .increment();
    }

    private String bounded(String value, Set<String> allowed) {
        if (value == null) {
            return "other";
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return allowed.contains(normalized) ? normalized : "other";
    }
}
