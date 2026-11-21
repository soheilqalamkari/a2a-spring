package io.a2aspring.observability;

import static org.assertj.core.api.Assertions.assertThat;

import io.a2aspring.web.A2AWebMetricsRecorder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class A2ASpringMetricsRecorderTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final A2ASpringMetricsRecorder recorder = new A2ASpringMetricsRecorder(registry);

    @Test
    void recordsRequestCountAndDurationWithBoundedTags() {
        A2AWebMetricsRecorder.RequestObservation observation = recorder.startRequest("private/custom/method");
        observation.complete("unexpected-user-supplied-outcome");

        assertThat(registry.get("a2a.server.requests").tag("method", "other").tag("outcome", "other")
                .counter().count()).isEqualTo(1.0);
        assertThat(registry.get("a2a.server.request.duration").tag("method", "other").tag("outcome", "other")
                .timer().count()).isEqualTo(1);
        assertThat(registry.getMeters()).hasSize(3);
    }

    @Test
    void tracksStreamLifetimeAndMakesCompletionIdempotent() {
        A2AWebMetricsRecorder.StreamObservation stream = recorder.startRequest("message/stream").startStream();

        assertThat(registry.get("a2a.server.streams.active").gauge().value()).isEqualTo(1.0);
        stream.complete("success");
        stream.complete("error");

        assertThat(registry.get("a2a.server.streams.active").gauge().value()).isZero();
        assertThat(registry.get("a2a.server.stream.duration").tag("outcome", "success").timer().count())
                .isEqualTo(1);
        assertThat(registry.getMeters()).hasSize(2);
    }

    @Test
    void recordsOnlyFiniteTaskOutcomeTagValues() {
        recorder.recordTaskOutcome("completed");
        recorder.recordTaskOutcome("tenant-123-user-generated-state");

        assertThat(registry.get("a2a.server.task.outcomes").tag("outcome", "completed").counter().count())
                .isEqualTo(1.0);
        assertThat(registry.get("a2a.server.task.outcomes").tag("outcome", "other").counter().count())
                .isEqualTo(1.0);
    }
}
