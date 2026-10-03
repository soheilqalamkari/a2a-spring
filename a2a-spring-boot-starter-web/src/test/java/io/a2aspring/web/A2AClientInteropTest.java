package io.a2aspring.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.a2aproject.sdk.client.http.JdkA2AHttpClient;
import org.a2aproject.sdk.client.transport.jsonrpc.JSONRPCTransport;
import org.a2aproject.sdk.client.transport.spi.interceptors.ClientCallContext;
import org.a2aproject.sdk.jsonrpc.common.json.JsonUtil;
import org.a2aproject.sdk.jsonrpc.common.wrappers.SendStreamingMessageRequest;
import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.server.agentexecution.RequestContext;
import org.a2aproject.sdk.server.tasks.AgentEmitter;
import org.a2aproject.sdk.spec.AgentCard;
import org.a2aproject.sdk.spec.AgentInterface;
import org.a2aproject.sdk.spec.EventKind;
import org.a2aproject.sdk.spec.Message;
import org.a2aproject.sdk.spec.MessageSendParams;
import org.a2aproject.sdk.spec.TextPart;
import org.a2aproject.sdk.spec.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;

@SpringBootTest(
        classes = A2AClientInteropTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class A2AClientInteropTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void officialJsonRpcClientCanCallSpringServer() throws Exception {
        String cardJson = restTemplate.getForObject(
                "http://localhost:" + port + "/.well-known/agent-card.json", String.class);
        AgentCard discoveredCard = JsonUtil.fromJson(cardJson, AgentCard.class);

        String endpoint = "http://localhost:" + port + "/a2a";
        AgentInterface jsonRpcInterface = new AgentInterface("JSONRPC", endpoint);
        AgentCard clientCard = AgentCard.builder(discoveredCard)
                .url(endpoint)
                .supportedInterfaces(List.of(jsonRpcInterface))
                .build();

        JSONRPCTransport transport = new JSONRPCTransport(
                new JdkA2AHttpClient(), clientCard, jsonRpcInterface, List.of());
        try {
            Message message = Message.builder()
                    .role(Message.Role.ROLE_USER)
                    .messageId("interop-message")
                    .contextId("interop-context")
                    .parts(new TextPart("hello from the official client"))
                    .build();
            EventKind response = transport.sendMessage(
                    MessageSendParams.builder().message(message).build(),
                    new ClientCallContext(Map.of(), Map.of("A2A-Version", "1.0")));

            assertThat(response).isInstanceOf(Task.class);
            Task task = (Task) response;
            assertThat(task.artifacts()).isNotEmpty();
            assertThat(task.artifacts().get(0).parts().get(0))
                    .isInstanceOfSatisfying(TextPart.class,
                            part -> assertThat(part.text()).isEqualTo("Hello from interop agent"));
        } finally {
            transport.close();
        }
    }

    @Test
    void streamingJsonRpcReturnsServerSentEvents() throws Exception {
        Message message = Message.builder()
                .role(Message.Role.ROLE_USER)
                .messageId("stream-message")
                .contextId("stream-context")
                .parts(new TextPart("stream hello"))
                .build();
        MessageSendParams params = MessageSendParams.builder().message(message).build();
        String requestBody = JsonUtil.toJson(new SendStreamingMessageRequest("stream-request", params));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/a2a"))
                .header("Content-Type", "application/json")
                .header("Accept", "text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("content-type")).hasValueSatisfying(
                value -> assertThat(value).contains("text/event-stream"));
        assertThat(response.body()).contains("data:", "Hello from interop agent");
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
                emitter.addArtifact(List.of(new TextPart("Hello from interop agent")));
                emitter.complete();
            };
        }
    }
}
