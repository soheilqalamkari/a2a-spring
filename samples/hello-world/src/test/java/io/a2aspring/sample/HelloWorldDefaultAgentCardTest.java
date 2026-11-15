package io.a2aspring.sample;

import static org.assertj.core.api.Assertions.assertThat;

import org.a2aproject.sdk.spec.AgentCard;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = HelloWorldApplication.class)
class HelloWorldDefaultAgentCardTest {

    @Autowired
    private AgentCard agentCard;

    @Test
    void doesNotAdvertiseExtensionsInTheDefaultProfile() {
        assertThat(agentCard.capabilities().extensions()).isEmpty();
    }
}
