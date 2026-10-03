package io.a2aspring.sample;

import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.server.agentexecution.RequestContext;
import org.a2aproject.sdk.server.tasks.AgentEmitter;
import org.a2aproject.sdk.spec.TextPart;
import org.a2aproject.sdk.spec.DataPart;
import org.a2aproject.sdk.spec.FilePart;
import org.a2aproject.sdk.spec.FileWithUri;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class HelloWorldAgent implements AgentExecutor {
    @Override
    public void cancel(RequestContext context, AgentEmitter emitter) {
        emitter.cancel();
    }

    @Override
    public void execute(RequestContext context, AgentEmitter emitter) {
        String messageId = context.getMessage().messageId();
        if (context.getTask() == null) {
            emitter.submit();
        }
        if (messageId.contains("input-required")) {
            emitter.startWork();
            emitter.requiresInput();
            return;
        }
        if (messageId.contains("auth-required")) {
            emitter.startWork();
            emitter.requiresAuth();
            return;
        }
        if (messageId.contains("reject")) {
            emitter.reject();
            return;
        }
        emitter.startWork();
        if (messageId.contains("artifact-text")) {
            emitter.addArtifact(List.of(new TextPart("Generated text content")));
        } else if (messageId.contains("artifact-data")) {
            emitter.addArtifact(List.of(new DataPart(Map.of("key", "value", "count", 42))));
        } else if (messageId.contains("artifact-file")) {
            emitter.addArtifact(List.of(new FilePart(
                    new FileWithUri("text/plain", "output.txt", "https://example.com/output.txt"))));
        } else if (messageId.contains("message-response")) {
            emitter.sendMessage(List.of(new TextPart("Generated message response")));
            emitter.complete();
        } else {
            emitter.addArtifact(List.of(new TextPart("Hello from a Spring Boot A2A agent")));
        }
        emitter.complete();
    }
}
