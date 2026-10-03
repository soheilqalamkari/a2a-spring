package io.a2aspring.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Flow;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.a2aproject.sdk.jsonrpc.common.json.JsonUtil;
import org.a2aproject.sdk.jsonrpc.common.wrappers.CancelTaskRequest;
import org.a2aproject.sdk.jsonrpc.common.wrappers.CreateTaskPushNotificationConfigRequest;
import org.a2aproject.sdk.jsonrpc.common.wrappers.DeleteTaskPushNotificationConfigRequest;
import org.a2aproject.sdk.jsonrpc.common.wrappers.GetExtendedAgentCardRequest;
import org.a2aproject.sdk.jsonrpc.common.wrappers.GetTaskRequest;
import org.a2aproject.sdk.jsonrpc.common.wrappers.GetTaskPushNotificationConfigRequest;
import org.a2aproject.sdk.jsonrpc.common.wrappers.ListTaskPushNotificationConfigsRequest;
import org.a2aproject.sdk.jsonrpc.common.wrappers.ListTasksRequest;
import org.a2aproject.sdk.jsonrpc.common.wrappers.SendMessageRequest;
import org.a2aproject.sdk.jsonrpc.common.wrappers.SendStreamingMessageRequest;
import org.a2aproject.sdk.jsonrpc.common.wrappers.SendStreamingMessageResponse;
import org.a2aproject.sdk.jsonrpc.common.wrappers.SubscribeToTaskRequest;
import org.a2aproject.sdk.server.ServerCallContext;
import org.a2aproject.sdk.transport.jsonrpc.handler.JSONRPCHandler;

/** Dispatches JSON-RPC requests to the official A2A SDK handler. */
public final class JsonRpcRequestDispatcher {
    private final JSONRPCHandler handler;

    public JsonRpcRequestDispatcher(JSONRPCHandler handler) {
        this.handler = handler;
    }

    public String dispatch(String request, ServerCallContext context) throws Exception {
        JsonObject root = root(request);
        String method = method(root);
        Object response = switch (method) {
            case "message/send", "SendMessage" -> handler.onMessageSend(
                    JsonUtil.fromJson(request, SendMessageRequest.class), context);
            case "tasks/get", "GetTask" -> handler.onGetTask(
                    JsonUtil.fromJson(request, GetTaskRequest.class), context);
            case "tasks/cancel", "CancelTask" -> handler.onCancelTask(
                    JsonUtil.fromJson(request, CancelTaskRequest.class), context);
            case "tasks/list", "ListTasks" -> handler.onListTasks(
                    JsonUtil.fromJson(request, ListTasksRequest.class), context);
            case "tasks/pushNotificationConfig/set", "CreateTaskPushNotificationConfig" ->
                    handler.setPushNotificationConfig(
                            JsonUtil.fromJson(request, CreateTaskPushNotificationConfigRequest.class), context);
            case "tasks/pushNotificationConfig/get", "GetTaskPushNotificationConfig" ->
                    handler.getPushNotificationConfig(
                            JsonUtil.fromJson(request, GetTaskPushNotificationConfigRequest.class), context);
            case "tasks/pushNotificationConfig/list", "ListTaskPushNotificationConfigs" ->
                    handler.listPushNotificationConfigs(
                            JsonUtil.fromJson(request, ListTaskPushNotificationConfigsRequest.class), context);
            case "tasks/pushNotificationConfig/delete", "DeleteTaskPushNotificationConfig" ->
                    handler.deletePushNotificationConfig(
                            JsonUtil.fromJson(request, DeleteTaskPushNotificationConfigRequest.class), context);
            case "agent/card", "GetExtendedAgentCard" -> handler.onGetExtendedCardRequest(
                    JsonUtil.fromJson(request, GetExtendedAgentCardRequest.class), context);
            default -> methodNotFound(root, method);
        };
        return JsonUtil.toJson(response);
    }

    public boolean isStreaming(String request) {
        String method = method(root(request));
        return "message/stream".equals(method)
                || "SendStreamingMessage".equals(method)
                || "tasks/resubscribe".equals(method)
                || "SubscribeToTask".equals(method);
    }

    public Flow.Publisher<SendStreamingMessageResponse> dispatchStreaming(
            String request, ServerCallContext context) throws Exception {
        String method = method(root(request));
        return switch (method) {
            case "message/stream", "SendStreamingMessage" -> handler.onMessageSendStream(
                    JsonUtil.fromJson(request, SendStreamingMessageRequest.class), context);
            case "tasks/resubscribe", "SubscribeToTask" -> handler.onSubscribeToTask(
                    JsonUtil.fromJson(request, SubscribeToTaskRequest.class), context);
            default -> throw new IllegalArgumentException("Unsupported streaming method: " + method);
        };
    }

    private JsonObject root(String request) {
        return JsonParser.parseString(request).getAsJsonObject();
    }

    private String method(JsonObject root) {
        return root.has("method") ? root.get("method").getAsString() : "";
    }

    private Map<String, Object> methodNotFound(JsonObject root, String method) {
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("jsonrpc", "2.0");
        errorResponse.put("id", root.has("id")
                ? JsonUtil.OBJECT_MAPPER.fromJson(root.get("id"), Object.class) : null);
        errorResponse.put("error", Map.of("code", -32601,
                "message", "Unsupported A2A JSON-RPC method: " + method));
        return errorResponse;
    }
}
