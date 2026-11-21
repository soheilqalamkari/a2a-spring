package io.a2aspring.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import io.a2aspring.web.A2AWebMetricsRecorder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.server.agentexecution.RequestContext;
import org.a2aproject.sdk.server.tasks.AgentEmitter;
import org.a2aproject.sdk.spec.TextPart;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(classes = A2ASpringMetricsIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
class A2ASpringMetricsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private A2AWebMetricsRecorder metricsRecorder;

    @Test
    void instrumentsJsonRpcRequestsAndTerminalTaskOutcomes() throws Exception {
        String request = """
                {"jsonrpc":"2.0","id":1,"method":"message/send","params":{"message":{"messageId":"m1","contextId":"c1","role":"ROLE_USER","parts":[{"kind":"text","text":"hello"}]}}}
                """;

        mockMvc.perform(post("/a2a").header("A2A-Version", "1.0")
                        .contentType("application/json").content(request))
                .andExpect(status().isOk());

        assertThat(metricsRecorder).isInstanceOf(A2ASpringMetricsRecorder.class);
        assertThat(meterRegistry.get("a2a.server.requests")
                .tag("method", "message/send").tag("outcome", "success").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("a2a.server.request.duration")
                .tag("method", "message/send").tag("outcome", "success").timer().count()).isEqualTo(1);
        assertThat(meterRegistry.get("a2a.server.task.outcomes")
                .tag("outcome", "completed").counter().count()).isEqualTo(1.0);
    }

    @Test
    void tracksSseStreamUntilTheEmitterCompletes() throws Exception {
        String request = """
                {"jsonrpc":"2.0","id":2,"method":"message/stream","params":{"message":{"messageId":"m2","contextId":"c2","role":"ROLE_USER","parts":[{"kind":"text","text":"hello"}]}}}
                """;

        MvcResult result = mockMvc.perform(post("/a2a").header("A2A-Version", "1.0")
                        .accept("text/event-stream").contentType("application/json").content(request))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/event-stream"))
                .andReturn();
        if (result.getRequest().isAsyncStarted()) {
            mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk());
        }

        assertThat(meterRegistry.get("a2a.server.streams.active").gauge().value()).isZero();
        assertThat(meterRegistry.get("a2a.server.stream.duration").tag("outcome", "success").timer().count())
                .isEqualTo(1);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }

        @Bean
        AgentExecutor agentExecutor() {
            return (RequestContext context, AgentEmitter emitter) -> {
                if (context.getTask() == null) {
                    emitter.submit();
                }
                emitter.startWork();
                emitter.addArtifact(List.of(new TextPart("metrics-test")));
                emitter.complete();
            };
        }
    }
}
