package io.a2aspring.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.a2aproject.sdk.server.ServerCallContext;
import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.server.agentexecution.RequestContext;
import org.a2aproject.sdk.server.auth.AuthenticatedUser;
import org.a2aproject.sdk.server.auth.TaskAuthorizationProvider;
import org.a2aproject.sdk.server.auth.TaskOperation;
import org.a2aproject.sdk.server.tasks.AgentEmitter;
import org.a2aproject.sdk.spec.TextPart;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = A2ASpringSecurityServerIntegrationTest.TestApplication.class,
        properties = "a2a.security.enabled=true")
@AutoConfigureMockMvc
class A2ASpringSecurityServerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecordingTaskAuthorizationProvider authorizationProvider;

    @Test
    void passesMappedAuthenticatedUserToSdkAuthorizationProvider() throws Exception {
        String request = """
                {"jsonrpc":"2.0","id":1,"method":"message/send","params":{"message":{"messageId":"security-test","role":"ROLE_USER","parts":[{"kind":"text","text":"hello"}]}}}
                """;
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                "alice", "must-not-be-copied", List.of());

        mockMvc.perform(post("/a2a")
                        .principal(authentication)
                        .header("A2A-Version", "1.0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("secured request accepted")));

        assertThat(authorizationProvider.createCalls()).isPositive();
        ServerCallContext context = authorizationProvider.lastContext().get();
        assertThat(context).isNotNull();
        assertThat(context.getUser()).isInstanceOf(AuthenticatedUser.class);
        assertThat(context.getUser().getUsername()).isEqualTo("alice");
        assertThat(context.getUser().getAttribute("credentials")).isNull();
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
                emitter.addArtifact(List.of(new TextPart("secured request accepted")));
                emitter.complete();
            };
        }

        @Bean
        RecordingTaskAuthorizationProvider taskAuthorizationProvider() {
            return new RecordingTaskAuthorizationProvider();
        }
    }

    static final class RecordingTaskAuthorizationProvider implements TaskAuthorizationProvider {
        private final AtomicInteger createCalls = new AtomicInteger();
        private final AtomicReference<ServerCallContext> lastContext = new AtomicReference<>();

        @Override
        public boolean checkRead(ServerCallContext context, String taskId, TaskOperation operation) {
            lastContext.set(context);
            return true;
        }

        @Override
        public boolean checkWrite(ServerCallContext context, String taskId, TaskOperation operation) {
            lastContext.set(context);
            return true;
        }

        @Override
        public boolean checkCreate(ServerCallContext context, TaskOperation operation) {
            createCalls.incrementAndGet();
            lastContext.set(context);
            return true;
        }

        @Override
        public boolean isTaskRecorded(String taskId) {
            return false;
        }

        @Override
        public void recordOwnership(ServerCallContext context, String taskId, TaskOperation operation) {
            lastContext.set(context);
        }

        int createCalls() {
            return createCalls.get();
        }

        AtomicReference<ServerCallContext> lastContext() {
            return lastContext;
        }
    }
}
