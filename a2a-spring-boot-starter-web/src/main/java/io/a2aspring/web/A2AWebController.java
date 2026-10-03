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

    public A2AWebController(JsonRpcRequestDispatcher dispatcher,
                            ServerCallContextFactory contextFactory) {
        this.dispatcher = dispatcher;
        this.contextFactory = contextFactory;
    }

    @PostMapping(value = {"/a2a", "/a2a/", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> jsonRpc(@RequestBody String request, jakarta.servlet.http.HttpServletRequest servletRequest) throws Exception {
        var context = contextFactory.create(servletRequest);
        try {
            if (!dispatcher.isStreaming(request)) {
                String response = dispatcher.dispatch(request, context);
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
            }

            ResponseBodyEmitter emitter = new ResponseBodyEmitter();
            dispatcher.dispatchStreaming(request, context).subscribe(new java.util.concurrent.Flow.Subscriber<>() {
                @Override
                public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(org.a2aproject.sdk.jsonrpc.common.wrappers.SendStreamingMessageResponse response) {
                    try {
                        emitter.send("data: " + JsonUtil.toJson(response) + "\n\n", MediaType.TEXT_PLAIN);
                    } catch (Exception exception) {
                        emitter.completeWithError(exception);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    emitter.completeWithError(throwable);
                }

                @Override
                public void onComplete() {
                    emitter.complete();
                }
            });
            return ResponseEntity.ok().contentType(MediaType.TEXT_EVENT_STREAM).body(emitter);
        } catch (RuntimeException exception) {
            int code = exception instanceof com.google.gson.JsonParseException ? -32700 : -32602;
            String message = code == -32700 ? "Parse error" : "Invalid params";
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(JsonUtil.toJson(errorResponse(request, code, message)));
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
        response.put("error", Map.of("code", code, "message", message));
        return response;
    }
}
