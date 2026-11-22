package io.a2aspring.tutorial;

import java.util.List;
import java.util.Locale;
import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.server.agentexecution.RequestContext;
import org.a2aproject.sdk.server.tasks.AgentEmitter;
import org.a2aproject.sdk.spec.TextPart;
import org.springframework.stereotype.Component;

@Component
public class HelpDeskAgent implements AgentExecutor {

    @Override
    public void cancel(RequestContext context, AgentEmitter emitter) {
        emitter.cancel();
    }

    @Override
    public void execute(RequestContext context, AgentEmitter emitter) {
        if (context.getTask() == null) {
            emitter.submit();
        }

        String question = context.getMessage().parts().stream()
                .filter(TextPart.class::isInstance)
                .map(TextPart.class::cast)
                .map(TextPart::text)
                .findFirst()
                .orElse("");

        emitter.startWork();
        emitter.addArtifact(List.of(new TextPart(answerFor(question))));
        emitter.complete();
    }

    private String answerFor(String question) {
        String normalized = question.toLowerCase(Locale.ROOT);
        if (normalized.contains("open") || normalized.contains("hours")) {
            return "The help desk is open Monday to Friday, 09:00–17:00 UTC.";
        }
        if (normalized.contains("password")) {
            return "Use your organization's password-reset page. If you are still locked out, contact the help desk.";
        }
        return "I can help with support hours or password reset. What would you like to know?";
    }
}
