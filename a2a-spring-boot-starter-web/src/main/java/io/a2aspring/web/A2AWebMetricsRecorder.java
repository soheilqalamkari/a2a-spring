package io.a2aspring.web;

/** Optional observation hook for the Spring MVC transport lifecycle. */
public interface A2AWebMetricsRecorder {

    RequestObservation startRequest(String method);

    void recordTaskOutcome(String outcome);

    interface RequestObservation {
        void complete(String outcome);

        StreamObservation startStream();
    }

    interface StreamObservation {
        void complete(String outcome);
    }
}
