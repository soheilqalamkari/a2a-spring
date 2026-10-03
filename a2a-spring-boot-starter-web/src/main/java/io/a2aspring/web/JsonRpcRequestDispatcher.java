package io.a2aspring.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Flow;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.Instant;
import java.util.HashMap;
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
import org.a2aproject.sdk.spec.CancelTaskParams;
import org.a2aproject.sdk.spec.ListTasksParams;
import org.a2aproject.sdk.spec.TaskIdParams;
import org.a2aproject.sdk.spec.TaskState;
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
                    cancelTaskRequest(root), context);
            case "tasks/list", "ListTasks" -> handler.onListTasks(
                    listTasksRequest(root), context);
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
        return normalizeResponse(JsonUtil.toJson(response));
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
        JsonObject root = root(request);
        String method = method(root);
        return switch (method) {
            case "message/stream", "SendStreamingMessage" -> handler.onMessageSendStream(
                    JsonUtil.fromJson(request, SendStreamingMessageRequest.class), context);
            case "tasks/resubscribe", "SubscribeToTask" -> handler.onSubscribeToTask(
                    subscribeToTaskRequest(root), context);
            default -> throw new IllegalArgumentException("Unsupported streaming method: " + method);
        };
    }

    private JsonObject root(String request) {
        return JsonParser.parseString(request).getAsJsonObject();
    }

    private String method(JsonObject root) {
        return root.has("method") ? root.get("method").getAsString() : "";
    }

    private CancelTaskRequest cancelTaskRequest(JsonObject root) {
        JsonObject params = params(root);
        CancelTaskParams.Builder builder = CancelTaskParams.builder()
                .id(requiredString(params, "id"));
        if (params.has("tenant") && !params.get("tenant").isJsonNull()) {
            builder.tenant(params.get("tenant").getAsString());
        }
        if (params.has("metadata") && params.get("metadata").isJsonObject()) {
            Map<String, Object> metadata = new HashMap<>();
            params.getAsJsonObject("metadata").entrySet().forEach(entry ->
                    metadata.put(entry.getKey(), JsonUtil.OBJECT_MAPPER.fromJson(entry.getValue(), Object.class)));
            builder.metadata(metadata);
        }
        return CancelTaskRequest.builder()
                .jsonrpc(jsonrpc(root))
                .id(requestId(root))
                .params(builder.build())
                .build();
    }

    private ListTasksRequest listTasksRequest(JsonObject root) {
        JsonObject params = params(root);
        ListTasksParams.Builder builder = ListTasksParams.builder()
                .contextId(requiredString(params, "context_id"));
        optionalString(params, "tenant", builder::tenant);
        optionalString(params, "page_token", builder::pageToken);
        optionalInteger(params, "page_size", builder::pageSize);
        optionalInteger(params, "history_length", builder::historyLength);
        optionalBoolean(params, "include_artifacts", builder::includeArtifacts);
        optionalString(params, "status_timestamp_after",
                value -> builder.statusTimestampAfter(Instant.parse(value)));
        optionalString(params, "status", value -> builder.status(TaskState.valueOf(value)));
        return ListTasksRequest.builder()
                .jsonrpc(jsonrpc(root))
                .id(requestId(root))
                .params(builder.build())
                .build();
    }

    private SubscribeToTaskRequest subscribeToTaskRequest(JsonObject root) {
        JsonObject params = params(root);
        TaskIdParams.Builder builder = TaskIdParams.builder()
                .id(requiredString(params, "id"));
        optionalString(params, "tenant", builder::tenant);
        return SubscribeToTaskRequest.builder()
                .jsonrpc(jsonrpc(root))
                .id(requestId(root))
                .params(builder.build())
                .build();
    }

    private JsonObject params(JsonObject root) {
        if (!root.has("params") || !root.get("params").isJsonObject()) {
            throw new IllegalArgumentException("params must be an object");
        }
        return root.getAsJsonObject("params");
    }

    private String requiredString(JsonObject object, String name) {
        if (!object.has(name) || object.get(name).isJsonNull()) {
            throw new IllegalArgumentException("Missing required parameter: " + name);
        }
        return object.get(name).getAsString();
    }

    private void optionalString(JsonObject object, String name, java.util.function.Consumer<String> consumer) {
        if (object.has(name) && !object.get(name).isJsonNull()) {
            consumer.accept(object.get(name).getAsString());
        }
    }

    private void optionalInteger(JsonObject object, String name, java.util.function.Consumer<Integer> consumer) {
        if (object.has(name) && !object.get(name).isJsonNull()) {
            consumer.accept(object.get(name).getAsInt());
        }
    }

    private void optionalBoolean(JsonObject object, String name, java.util.function.Consumer<Boolean> consumer) {
        if (object.has(name) && !object.get(name).isJsonNull()) {
            consumer.accept(object.get(name).getAsBoolean());
        }
    }

    private String jsonrpc(JsonObject root) {
        return root.has("jsonrpc") ? root.get("jsonrpc").getAsString() : "2.0";
    }

    private Object requestId(JsonObject root) {
        return root.has("id") ? JsonUtil.OBJECT_MAPPER.fromJson(root.get("id"), Object.class) : null;
    }

    private String normalizeResponse(String serialized) throws Exception {
        JsonObject response = JsonParser.parseString(serialized).getAsJsonObject();
        if (!response.has("error")) {
            return serialized;
        }
        if (!response.get("error").isJsonObject()) {
            JsonObject error = new JsonObject();
            error.addProperty("code", -32006);
            error.addProperty("message", response.get("error").getAsString());
            response.add("error", error);
        }
        JsonObject error = response.getAsJsonObject("error");
        if (!error.has("data")) {
            int code = error.has("code") ? error.get("code").getAsInt() : -32006;
            String reason = switch (code) {
                case -32001 -> "TASK_NOT_FOUND";
                case -32002 -> "TASK_NOT_CANCELABLE";
                case -32003 -> "PUSH_NOTIFICATION_NOT_SUPPORTED";
                case -32004 -> "UNSUPPORTED_OPERATION";
                case -32005 -> "CONTENT_TYPE_NOT_SUPPORTED";
                case -32009 -> "VERSION_NOT_SUPPORTED";
                default -> "INVALID_PARAMS";
            };
            JsonObject info = new JsonObject();
            info.addProperty("@type", "type.googleapis.com/google.rpc.ErrorInfo");
            info.addProperty("reason", reason);
            info.addProperty("domain", "a2a-protocol.org");
            error.add("data", JsonParser.parseString("[" + info + "]"));
        }
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
