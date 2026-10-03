package io.a2aspring.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import io.a2aspring.core.A2ARuntime;
import jakarta.servlet.http.HttpServletRequest;
import org.a2aproject.sdk.jsonrpc.common.json.JsonUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.a2aproject.sdk.jsonrpc.common.wrappers.SendMessageRequest;
import org.a2aproject.sdk.server.ServerCallContext;
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
    private final A2ARuntime runtime;

    public A2AWebController(JSONRPCHandler handler, A2ARuntime runtime) {
        this.handler = handler;
        this.runtime = runtime;
    }

    @PostMapping(value = "/a2a", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> jsonRpc(@RequestBody String request, HttpServletRequest servletRequest)
            throws Exception {
        JsonObject root = JsonParser.parseString(request).getAsJsonObject();
        String method = root.has("method") ? root.get("method").getAsString() : "";
        // The SDK's current JSON-RPC client uses the generated operation name
        // "SendMessage", while the protocol's HTTP examples use "message/send".
        // Both forms represent the same operation and are accepted here so the
        // Spring adapter interoperates with both clients.
        if (!"message/send".equals(method) && !"SendMessage".equals(method)) {
            java.util.Map<String, Object> errorResponse = new java.util.LinkedHashMap<>();
            errorResponse.put("jsonrpc", "2.0");
            errorResponse.put("id", root.has("id")
                    ? JsonUtil.OBJECT_MAPPER.fromJson(root.get("id"), Object.class) : null);
            errorResponse.put("error", java.util.Map.of("code", -32601,
                    "message", "Unsupported A2A JSON-RPC method: " + method));
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(JsonUtil.toJson(errorResponse));
        }
        SendMessageRequest messageRequest = JsonUtil.fromJson(request, SendMessageRequest.class);
        Object response = handler.onMessageSend(messageRequest, context(servletRequest));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(JsonUtil.toJson(response));
    }

    private ServerCallContext context(HttpServletRequest request) {
        Map<String, Object> state = new HashMap<>();
        state.put(ServerCallContext.TRANSPORT_KEY, "jsonrpc");
        Map<String, String> headers = Collections.list(request.getHeaderNames())
                .stream().collect(java.util.stream.Collectors.toMap(
                        name -> name, request::getHeader, (left, right) -> left));
        state.put("headers", headers);
        String protocolVersion = request.getHeader("A2A-Version");
        return new ServerCallContext(null, state, java.util.Set.of(),
                protocolVersion == null ? "1.0" : protocolVersion);
    }
}
