package io.a2aspring.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.server.agentexecution.RequestContext;
import org.a2aproject.sdk.server.tasks.AgentEmitter;
import org.a2aproject.sdk.spec.TextPart;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest(classes = A2AWebIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
class A2AWebIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void servesAgentCard() throws Exception {
        mockMvc.perform(get("/.well-known/agent-card.json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("spring-a2a-agent")));
    }

    @Test
    void handlesJsonRpcMessageSend() throws Exception {
        String request = """
                {"jsonrpc":"2.0","id":1,"method":"message/send","params":{"message":{"messageId":"m1","contextId":"c1","role":"ROLE_USER","parts":[{"kind":"text","text":"hello"}]}}}
                """;

        mockMvc.perform(post("/a2a").header("A2A-Version", "1.0")
                        .contentType("application/json").content(request))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Hello from test agent")));
    }

    @Test
    void returnsJsonRpcMethodNotFoundError() throws Exception {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"unknown/method\",\"params\":{}}";

        mockMvc.perform(post("/a2a").contentType("application/json").content(request))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("-32601")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("unknown/method")));
    }

    @Test
    void dispatchesTaskGetToOfficialSdkHandler() throws Exception {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tasks/get\",\"params\":{\"id\":\"missing-task\"}}";

        mockMvc.perform(post("/a2a").contentType("application/json").content(request))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("-32001")));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
        @Bean
        AgentExecutor agentExecutor() {
            return (RequestContext context, AgentEmitter emitter) -> {
                if (context.getTask() == null) {
                    emitter.submit();
                }
                emitter.startWork();
                emitter.addArtifact(List.of(new TextPart("Hello from test agent")));
                emitter.complete();
            };
        }
    }
}
