package io.a2aspring.web;

import io.a2aspring.core.A2ARuntime;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.LinkedHashMap;
import java.util.Map;
import org.a2aproject.sdk.jsonrpc.common.json.JsonUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnBean(A2ARuntime.class)
public class A2AWebController {
    private final JsonRpcRequestDispatcher dispatcher;
    private final ServerCallContextFactory contextFactory;
    private final A2AWebMetricsRecorder metricsRecorder;

    public A2AWebController(JsonRpcRequestDispatcher dispatcher,
                            ServerCallContextFactory contextFactory,
                            A2AWebMetricsRecorder metricsRecorder) {
        this.dispatcher = dispatcher;
        this.contextFactory = contextFactory;
        this.metricsRecorder = metricsRecorder;
    }

    @PostMapping(value = {"/a2a", "/a2a/", "/"})
    public ResponseEntity<?> jsonRpc(@RequestBody String request, jakarta.servlet.http.HttpServletRequest servletRequest) throws Exception {
        String method = metricsRecorder == null ? "other" : requestMethod(request);
        A2AWebMetricsRecorder.RequestObservation requestObservation = metricsRecorder == null
                ? null : metricsRecorder.startRequest(method);
        org.a2aproject.sdk.server.ServerCallContext context;
        try {
            context = contextFactory.create(servletRequest);
        } catch (Exception exception) {
            complete(requestObservation, "error");
            throw exception;
        }
        if (servletRequest.getContentType() == null
                || !servletRequest.getContentType().toLowerCase(java.util.Locale.ROOT)
                .startsWith(MediaType.APPLICATION_JSON_VALUE)) {
            complete(requestObservation, "unsupported_content_type");
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(JsonUtil.toJson(errorResponse(request, -32005,
                            "Content type is not supported")));
        }
        try {
            JsonParser.parseString(request).getAsJsonObject();
        } catch (RuntimeException exception) {
            complete(requestObservation, "parse_error");
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(JsonUtil.toJson(errorResponse(request, -32700, "Parse error")));
        }
        JsonObject root = JsonParser.parseString(request).getAsJsonObject();
        if (!root.has("jsonrpc") || !root.get("jsonrpc").isJsonPrimitive()
                || !root.getAsJsonPrimitive("jsonrpc").isString()
                || !"2.0".equals(root.get("jsonrpc").getAsString())
                || !root.has("method") || !root.get("method").isJsonPrimitive()
                || !root.getAsJsonPrimitive("method").isString()
                || root.get("method").getAsString().isBlank()
                || (root.has("id") && !root.get("id").isJsonNull()
                && (!root.get("id").isJsonPrimitive()
                || (!root.get("id").getAsJsonPrimitive().isString()
                && !root.get("id").getAsJsonPrimitive().isNumber())))) {
            complete(requestObservation, "invalid_request");
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(JsonUtil.toJson(errorResponse(request, -32600, "Invalid Request")));
        }
        try {
            if (!dispatcher.isStreaming(request)) {
                String response = dispatcher.dispatch(request, context);
                recordTaskOutcome(response);
                complete(requestObservation, responseOutcome(response));
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
            }

            var publisher = dispatcher.dispatchStreaming(request, context);
            ResponseBodyEmitter emitter = new ResponseBodyEmitter();
            A2AWebMetricsRecorder.StreamObservation streamObservation = requestObservation == null
                    ? null : requestObservation.startStream();
            complete(requestObservation, "streaming");
            java.util.concurrent.atomic.AtomicReference<String> streamOutcome =
                    new java.util.concurrent.atomic.AtomicReference<>("success");
            java.util.concurrent.atomic.AtomicBoolean streamFinished = new java.util.concurrent.atomic.AtomicBoolean();
            Runnable finishStream = () -> {
                if (streamObservation != null && streamFinished.compareAndSet(false, true)) {
                    streamObservation.complete(streamOutcome.get());
                }
            };
            emitter.onCompletion(finishStream);
            emitter.onTimeout(() -> {
                streamOutcome.set("timeout");
                finishStream.run();
            });
            emitter.onError(error -> {
                streamOutcome.set("error");
                finishStream.run();
            });
            publisher.subscribe(new java.util.concurrent.Flow.Subscriber<>() {
                @Override
                public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(org.a2aproject.sdk.jsonrpc.common.wrappers.SendStreamingMessageResponse response) {
                    try {
                        recordTaskOutcome(JsonUtil.toJson(response));
                        emitter.send("data: " + JsonUtil.toJson(response) + "\n\n", MediaType.TEXT_PLAIN);
                    } catch (Exception exception) {
                        streamOutcome.set("error");
                        finishStream.run();
                        emitter.completeWithError(exception);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    streamOutcome.set("error");
                    finishStream.run();
                    emitter.completeWithError(throwable);
                }

                @Override
                public void onComplete() {
                    emitter.complete();
                }
            });
            return ResponseEntity.ok().contentType(MediaType.TEXT_EVENT_STREAM).body(emitter);
        } catch (org.a2aproject.sdk.spec.A2AError exception) {
            complete(requestObservation, "protocol_error");
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(JsonUtil.toJson(errorResponse(request,
                            exception.getCode() == null ? -32006 : exception.getCode(),
                            exception.getMessage())));
        } catch (RuntimeException exception) {
            complete(requestObservation, "invalid_params");
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(JsonUtil.toJson(errorResponse(request, -32602, "Invalid params")));
        } catch (Exception exception) {
            complete(requestObservation, "error");
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(JsonUtil.toJson(errorResponse(request, -32602, "Invalid params")));
        }
    }

    private void complete(A2AWebMetricsRecorder.RequestObservation observation, String outcome) {
        if (observation != null) {
            observation.complete(outcome);
        }
    }

    private String requestMethod(String request) {
        try {
            JsonObject root = JsonParser.parseString(request).getAsJsonObject();
            return root.has("method") && root.get("method").isJsonPrimitive()
                    ? root.get("method").getAsString() : "other";
        } catch (RuntimeException exception) {
            return "other";
        }
    }

    private String responseOutcome(String response) {
        try {
            JsonObject root = JsonParser.parseString(response).getAsJsonObject();
            return root.has("error") && !root.get("error").isJsonNull() ? "protocol_error" : "success";
        } catch (RuntimeException exception) {
            return "error";
        }
    }

    private void recordTaskOutcome(String response) {
        if (metricsRecorder == null) {
            return;
        }
        try {
            JsonObject root = JsonParser.parseString(response).getAsJsonObject();
            JsonObject result = root.has("result") && root.get("result").isJsonObject()
                    ? root.getAsJsonObject("result") : root;
            if (result.has("task") && result.get("task").isJsonObject()) {
                result = result.getAsJsonObject("task");
            }
            JsonObject status = result.has("status") && result.get("status").isJsonObject()
                    ? result.getAsJsonObject("status") : null;
            if (status == null || !status.has("state") || !status.get("state").isJsonPrimitive()) {
                return;
            }
            String outcome = status.get("state").getAsString().toLowerCase(java.util.Locale.ROOT)
                    .replace("task_state_", "");
            if (java.util.Set.of("completed", "failed", "canceled", "rejected").contains(outcome)) {
                metricsRecorder.recordTaskOutcome(outcome);
            }
        } catch (RuntimeException ignored) {
            // Metrics must not change transport behavior for an unexpected response shape.
        }
    }

    private Map<String, Object> errorResponse(String request, int code, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("jsonrpc", "2.0");
        try {
            JsonObject root = JsonParser.parseString(request).getAsJsonObject();
            response.put("id", root.has("id") ? JsonUtil.OBJECT_MAPPER.fromJson(root.get("id"), Object.class) : null);
        } catch (RuntimeException ignored) {
            response.put("id", null);
        }
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", code);
        error.put("message", message);
        error.put("data", java.util.List.of(Map.of(
                "@type", "type.googleapis.com/google.rpc.ErrorInfo",
                "reason", switch (code) {
                    case -32001 -> "TASK_NOT_FOUND";
                    case -32002 -> "TASK_NOT_CANCELABLE";
                    case -32003 -> "PUSH_NOTIFICATION_NOT_SUPPORTED";
                    case -32004 -> "UNSUPPORTED_OPERATION";
                    case -32005 -> "CONTENT_TYPE_NOT_SUPPORTED";
                    case -32009 -> "VERSION_NOT_SUPPORTED";
                    default -> "INVALID_PARAMS";
                },
                "domain", "a2a-protocol.org")));
        response.put("error", error);
        return response;
    }
}
