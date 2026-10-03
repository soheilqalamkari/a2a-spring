package io.a2aspring.web;

import java.util.LinkedHashMap;
import java.util.Map;
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
import org.a2aproject.sdk.server.ServerCallContext;
import org.a2aproject.sdk.transport.jsonrpc.handler.JSONRPCHandler;

/** Dispatches JSON-RPC requests to the official A2A SDK handler. */
public final class JsonRpcRequestDispatcher {
    private final JSONRPCHandler handler;

    public JsonRpcRequestDispatcher(JSONRPCHandler handler) {
        this.handler = handler;
    }

    public String dispatch(String request, ServerCallContext context) throws Exception {
        JsonObject root = JsonParser.parseString(request).getAsJsonObject();
        String method = root.has("method") ? root.get("method").getAsString() : "";
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
