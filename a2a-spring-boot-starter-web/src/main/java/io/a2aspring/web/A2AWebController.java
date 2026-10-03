package io.a2aspring.web;

import io.a2aspring.core.A2ARuntime;
import org.a2aproject.sdk.jsonrpc.common.json.JsonUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.a2aproject.sdk.jsonrpc.common.wrappers.SendMessageRequest;
import org.a2aproject.sdk.transport.jsonrpc.handler.JSONRPCHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnBean(A2ARuntime.class)
public class A2AWebController {
    private final JSONRPCHandler handler;
    private final ServerCallContextFactory contextFactory;

    public A2AWebController(JSONRPCHandler handler, ServerCallContextFactory contextFactory) {
        this.handler = handler;
        this.contextFactory = contextFactory;
    }

    @PostMapping(value = "/a2a", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> jsonRpc(@RequestBody String request,
                                          jakarta.servlet.http.HttpServletRequest servletRequest) throws Exception {
        JsonObject root = JsonParser.parseString(request).getAsJsonObject();
        String method = root.has("method") ? root.get("method").getAsString() : "";
        // The SDK's current JSON-RPC client uses the generated operation name
        // "SendMessage", while the protocol's HTTP examples use "message/send".
        // Both forms represent the same operation and are accepted here so the
        // Spring adapter interoperates with both clients.
        if (!"message/send".equals(method) && !"SendMessage".equals(method)) {
            java.util.Map<String, Object> errorResponse = new java.util.LinkedHashMap<>();
            errorResponse.put("jsonrpc", "2.0");
            errorResponse.put("id", root.has("id") ? JsonUtil.OBJECT_MAPPER.fromJson(root.get("id"), Object.class) : null);
            errorResponse.put("error", java.util.Map.of("code", -32601, "message", "Unsupported A2A JSON-RPC method: " + method));
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(JsonUtil.toJson(errorResponse));
        }
        SendMessageRequest messageRequest = JsonUtil.fromJson(request, SendMessageRequest.class);
        Object response = handler.onMessageSend(messageRequest, contextFactory.create(servletRequest));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(JsonUtil.toJson(response));
    }
}
