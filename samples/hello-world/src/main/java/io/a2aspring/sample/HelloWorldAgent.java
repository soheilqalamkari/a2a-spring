package io.a2aspring.sample;

import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.server.agentexecution.RequestContext;
import org.a2aproject.sdk.server.tasks.AgentEmitter;
import org.a2aproject.sdk.spec.TextPart;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class HelloWorldAgent implements AgentExecutor {
    @Override
    public void execute(RequestContext context, AgentEmitter emitter) {
        if (context.getTask() == null) {
            emitter.submit();
        }
        emitter.startWork();
        emitter.addArtifact(List.of(new TextPart("Hello from a Spring Boot A2A agent")));
        emitter.complete();
    }
}
