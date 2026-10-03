package io.a2aspring.web;

import io.a2aspring.core.A2ARuntime;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnBean(A2ARuntime.class)
public class A2AAgentCardController {
    private final A2ARuntime runtime;

    public A2AAgentCardController(A2ARuntime runtime) {
        this.runtime = runtime;
    }

    @GetMapping(value = "/.well-known/agent-card.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object agentCard() {
        return runtime.agentCard();
    }
}
