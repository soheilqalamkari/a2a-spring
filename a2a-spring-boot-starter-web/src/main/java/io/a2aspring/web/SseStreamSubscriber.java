package io.a2aspring.web;

import java.util.Objects;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.a2aproject.sdk.jsonrpc.common.json.JsonUtil;
import org.a2aproject.sdk.jsonrpc.common.wrappers.SendStreamingMessageResponse;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/** Bridges an SDK stream to one HTTP response and releases the subscription when that response ends. */
final class SseStreamSubscriber implements Flow.Subscriber<SendStreamingMessageResponse> {
    private final ResponseBodyEmitter emitter;
    private final AtomicReference<Flow.Subscription> subscription = new AtomicReference<>();
    private final AtomicBoolean terminated = new AtomicBoolean();
    private final AtomicBoolean cancellationRequested = new AtomicBoolean();
    private final AtomicBoolean cancellationSent = new AtomicBoolean();

    SseStreamSubscriber(ResponseBodyEmitter emitter) {
        this.emitter = Objects.requireNonNull(emitter, "emitter");
        emitter.onCompletion(this::cancelIfActive);
        emitter.onTimeout(this::cancelIfActive);
        emitter.onError(error -> cancelIfActive());
    }

    @Override
    public void onSubscribe(Flow.Subscription newSubscription) {
        Objects.requireNonNull(newSubscription, "subscription");
        if (!subscription.compareAndSet(null, newSubscription)) {
            newSubscription.cancel();
            return;
        }

        if (cancellationRequested.get()) {
            cancelSubscription(newSubscription);
        } else if (terminated.get()) {
            newSubscription.cancel();
        } else {
            newSubscription.request(Long.MAX_VALUE);
        }
    }

    @Override
    public void onNext(SendStreamingMessageResponse response) {
        if (terminated.get()) {
            return;
        }
        try {
            emitter.send("data: " + JsonUtil.toJson(response) + "\n\n", MediaType.TEXT_PLAIN);
        } catch (Exception exception) {
            cancelIfActive();
            emitter.completeWithError(exception);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (terminated.compareAndSet(false, true)) {
            emitter.completeWithError(throwable);
        }
    }

    @Override
    public void onComplete() {
        if (terminated.compareAndSet(false, true)) {
            emitter.complete();
        }
    }

    private void cancelIfActive() {
        if (terminated.compareAndSet(false, true)) {
            cancellationRequested.set(true);
            Flow.Subscription current = subscription.get();
            if (current != null) {
                cancelSubscription(current);
            }
        }
    }

    private void cancelSubscription(Flow.Subscription current) {
        if (cancellationSent.compareAndSet(false, true)) {
            current.cancel();
        }
    }
}
