package io.a2aspring.sample;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.a2aproject.sdk.spec.AgentCard;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = HelloWorldApplication.class,
        properties = "spring.profiles.active=tck-required-extension")
@AutoConfigureMockMvc
class TckRequiredExtensionProfileTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgentCard agentCard;

    @Test
    void advertisesTheTckRequiredExtensionOnlyInTheScopedProfile() {
        assertThat(agentCard.capabilities().extensions())
                .singleElement()
                .satisfies(extension -> {
                    assertThat(extension.uri()).isEqualTo(TckRequiredExtensionConfiguration.EXTENSION_URI);
                    assertThat(extension.required()).isTrue();
                });
    }

    @Test
    void returnsExtensionSupportRequiredWhenRequestOmitsTheRequiredExtension() throws Exception {
        String request = """
                {"jsonrpc":"2.0","id":1,"method":"message/send","params":{"message":{"messageId":"tck-extension-test","role":"ROLE_USER","parts":[{"kind":"text","text":"hello"}]}}}
                """;

        mockMvc.perform(post("/a2a").header("A2A-Version", "1.0")
                        .contentType("application/json").content(request))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().string(containsString("-32008")))
                .andExpect(content().string(containsString(TckRequiredExtensionConfiguration.EXTENSION_URI)));
    }
}
