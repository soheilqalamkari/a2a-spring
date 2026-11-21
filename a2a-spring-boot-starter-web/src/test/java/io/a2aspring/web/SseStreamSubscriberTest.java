package io.a2aspring.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import org.a2aproject.sdk.jsonrpc.common.wrappers.SendStreamingMessageResponse;
import org.a2aproject.sdk.spec.Message;
import org.a2aproject.sdk.spec.TextPart;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

class SseStreamSubscriberTest {

    @Test
    void cancelsSubscriptionWhenResponseCompletes() {
        RecordingEmitter emitter = new RecordingEmitter();
        SseStreamSubscriber subscriber = new SseStreamSubscriber(emitter);
        RecordingSubscription subscription = new RecordingSubscription();
        subscriber.onSubscribe(subscription);

        emitter.completeResponse();

        assertThat(subscription.cancelCalls).isEqualTo(1);
    }

    @Test
    void cancelsSubscriptionWhenResponseTimesOut() {
        RecordingEmitter emitter = new RecordingEmitter();
        SseStreamSubscriber subscriber = new SseStreamSubscriber(emitter);
        RecordingSubscription subscription = new RecordingSubscription();
        subscriber.onSubscribe(subscription);

        emitter.timeout();

        assertThat(subscription.cancelCalls).isEqualTo(1);
    }

    @Test
    void cancelsWhenResponseEndsBeforeSubscriptionArrives() {
        RecordingEmitter emitter = new RecordingEmitter();
        SseStreamSubscriber subscriber = new SseStreamSubscriber(emitter);

        emitter.completeResponse();
        RecordingSubscription subscription = new RecordingSubscription();
        subscriber.onSubscribe(subscription);

        assertThat(subscription.cancelCalls).isEqualTo(1);
        assertThat(subscription.requested).isFalse();
    }

    @Test
    void cancelsWhenSendingAnEventFails() {
        RecordingEmitter emitter = new RecordingEmitter();
        IOException sendFailure = new IOException("client disconnected");
        emitter.sendFailure = sendFailure;
        SseStreamSubscriber subscriber = new SseStreamSubscriber(emitter);
        RecordingSubscription subscription = new RecordingSubscription();
        subscriber.onSubscribe(subscription);

        subscriber.onNext(streamingResponse());

        assertThat(subscription.cancelCalls).isEqualTo(1);
        assertThat(emitter.completionFailure).isSameAs(sendFailure);
    }

    @Test
    void normalPublisherCompletionDoesNotCancelFinishedSubscription() {
        RecordingEmitter emitter = new RecordingEmitter();
        SseStreamSubscriber subscriber = new SseStreamSubscriber(emitter);
        RecordingSubscription subscription = new RecordingSubscription();
        subscriber.onSubscribe(subscription);

        subscriber.onComplete();
        emitter.completeResponse();

        assertThat(subscription.cancelCalls).isZero();
        assertThat(emitter.completeCalls).isEqualTo(1);
    }

    @Test
    void cancelsSubscriptionWhenEmitterReportsAnError() {
        RecordingEmitter emitter = new RecordingEmitter();
        SseStreamSubscriber subscriber = new SseStreamSubscriber(emitter);
        RecordingSubscription subscription = new RecordingSubscription();
        subscriber.onSubscribe(subscription);

        emitter.error(new IOException("connection failed"));

        assertThat(subscription.cancelCalls).isEqualTo(1);
    }

    private SendStreamingMessageResponse streamingResponse() {
        Message message = Message.builder()
                .role(Message.Role.ROLE_AGENT)
                .parts(new TextPart("stream event"))
                .build();
        return new SendStreamingMessageResponse("stream-test", message);
    }

    private static final class RecordingSubscription implements Flow.Subscription {
        private int cancelCalls;
        private boolean requested;

        @Override
        public void request(long n) {
            requested = true;
        }

        @Override
        public void cancel() {
            cancelCalls++;
        }
    }

    private static final class RecordingEmitter extends ResponseBodyEmitter {
        private Runnable completion;
        private Runnable timeout;
        private Consumer<Throwable> error;
        private IOException sendFailure;
        private Throwable completionFailure;
        private int completeCalls;

        @Override
        public void send(Object object, MediaType mediaType) throws IOException {
            if (sendFailure != null) {
                throw sendFailure;
            }
        }

        @Override
        public void complete() {
            completeCalls++;
        }

        @Override
        public void completeWithError(Throwable failure) {
            completionFailure = failure;
        }

        @Override
        public void onCompletion(Runnable callback) {
            completion = callback;
        }

        @Override
        public void onTimeout(Runnable callback) {
            timeout = callback;
        }

        @Override
        public void onError(Consumer<Throwable> callback) {
            error = callback;
        }

        private void completeResponse() {
            completion.run();
        }

        private void timeout() {
            timeout.run();
        }

        private void error(Throwable failure) {
            error.accept(failure);
        }
    }
}
