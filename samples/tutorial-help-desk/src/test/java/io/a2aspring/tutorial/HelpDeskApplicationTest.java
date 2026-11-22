package io.a2aspring.tutorial;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class HelpDeskApplicationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publishesAgentCard() throws Exception {
        mockMvc.perform(get("/.well-known/agent-card.json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("help-desk-agent")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("JSONRPC")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"protocolVersion\":\"1.0\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"streaming\":true")));
    }

    @Test
    void answersAHelpDeskQuestionOverJsonRpc() throws Exception {
        String request = """
                {"jsonrpc":"2.0","id":1,"method":"message/send","params":{"message":{"messageId":"tutorial-message","contextId":"tutorial-context","role":"ROLE_USER","parts":[{"kind":"text","text":"What are your support hours?"}]}}}
                """;

        mockMvc.perform(post("/a2a")
                        .header("A2A-Version", "1.0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("The help desk is open Monday to Friday, 09:00–17:00 UTC.")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("COMPLETED")));
    }

    @Test
    void streamsTaskEventsOverServerSentEvents() throws Exception {
        String requestBody = """
                {"jsonrpc":"2.0","id":2,"method":"message/stream","params":{"message":{"messageId":"stream-question","contextId":"stream-session","role":"ROLE_USER","parts":[{"kind":"text","text":"How do I reset my password?"}]}}}
                """;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/a2a"))
                .header("Content-Type", "application/json")
                .header("A2A-Version", "1.0")
                .header("Accept", "text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        org.assertj.core.api.Assertions.assertThat(response.statusCode()).isEqualTo(200);
        org.assertj.core.api.Assertions.assertThat(response.headers().firstValue("content-type"))
                .hasValueSatisfying(value -> org.assertj.core.api.Assertions.assertThat(value)
                        .contains("text/event-stream"));
        org.assertj.core.api.Assertions.assertThat(response.body())
                .contains("data:", "TASK_STATE_COMPLETED", "password-reset page");
    }
}
