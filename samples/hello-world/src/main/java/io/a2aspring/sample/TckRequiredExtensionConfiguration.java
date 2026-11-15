package io.a2aspring.sample;

import java.util.List;
import java.util.Map;
import org.a2aproject.sdk.spec.AgentCapabilities;
import org.a2aproject.sdk.spec.AgentCard;
import org.a2aproject.sdk.spec.AgentExtension;
import org.a2aproject.sdk.spec.AgentInterface;
import io.a2aspring.core.A2AProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("tck-required-extension")
class TckRequiredExtensionConfiguration {

    static final String EXTENSION_URI = "urn:a2a:tck:required-extension";

    @Bean
    @ConditionalOnMissingBean(AgentCard.class)
    AgentCard tckRequiredExtensionAgentCard(A2AProperties properties) {
        AgentExtension extension = AgentExtension.builder()
                .uri(EXTENSION_URI)
                .required(true)
                .description("A2A TCK required-extension conformance profile")
                .params(Map.of())
                .build();

        String endpoint = properties.getAgent().getUrl();
        String transport = properties.getServer().getTransport();
        return AgentCard.builder()
                .name(properties.getAgent().getName())
                .description(properties.getAgent().getDescription())
                .version(properties.getAgent().getVersion())
                .capabilities(AgentCapabilities.builder()
                        .streaming(true)
                        .pushNotifications(false)
                        .extensions(List.of(extension))
                        .build())
                .defaultInputModes(List.of("text"))
                .defaultOutputModes(List.of("text"))
                .skills(List.of())
                .supportedInterfaces(List.of(new AgentInterface(transport, endpoint, "", "1.0")))
                .build();
    }
}
