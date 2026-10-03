package io.a2aspring.web;

import io.a2aspring.core.A2ARuntime;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnBean(A2ARuntime.class)
public class A2AAgentCardController {
    private final A2ARuntime runtime;
    private final ObjectMapper objectMapper;

    public A2AAgentCardController(A2ARuntime runtime, ObjectMapper objectMapper) {
        this.runtime = runtime;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/.well-known/agent-card.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> agentCard() throws com.fasterxml.jackson.core.JsonProcessingException {
        String json = objectMapper.copy().setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsString(runtime.agentCard());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
    }
}
