package io.a2aspring.autoconfigure;

import java.util.List;
import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.server.config.A2AConfigProvider;
import org.a2aproject.sdk.server.config.DefaultValuesConfigProvider;
import org.a2aproject.sdk.spec.AgentCapabilities;
import org.a2aproject.sdk.spec.AgentCard;
import org.a2aproject.sdk.spec.AgentInterface;
import io.a2aspring.core.A2AProperties;
import io.a2aspring.core.A2ARuntime;
import io.a2aspring.core.SpringEnvironmentA2AConfigProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
@ConditionalOnClass(AgentExecutor.class)
@EnableConfigurationProperties(A2AProperties.class)
public class A2AAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public A2AConfigProvider a2aConfigProvider(Environment environment, A2AProperties properties) {
        return new SpringEnvironmentA2AConfigProvider(environment, new DefaultValuesConfigProvider(),
                java.util.Map.of("a2a.authorization.required",
                        Boolean.toString(properties.getSecurity().isEnabled()),
                        "a2a.push-notification.enabled",
                        Boolean.toString(properties.getServer().isPushNotificationsEnabled())));
    }

    @Bean
    @ConditionalOnMissingBean
    public AgentCard a2aAgentCard(A2AProperties properties) {
        return AgentCard.builder()
                .name(properties.getAgent().getName())
                .description(properties.getAgent().getDescription())
                .version(properties.getAgent().getVersion())
                .capabilities(AgentCapabilities.builder()
                        .streaming(true)
                        .pushNotifications(properties.getServer().isPushNotificationsEnabled())
                        .extensions(List.of())
                        .build())
                .defaultInputModes(List.of("text"))
                .defaultOutputModes(List.of("text"))
                .skills(List.of())
                .supportedInterfaces(List.of(new AgentInterface(
                        properties.getServer().getTransport(), properties.getAgent().getUrl(), "", "1.0")))
                .build();
    }

    @Bean
    @ConditionalOnBean(AgentExecutor.class)
    @ConditionalOnProperty(prefix = "a2a.server", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public A2ARuntime a2aRuntime(A2AProperties properties, A2AConfigProvider provider, AgentExecutor executor, AgentCard agentCard) {
        return new A2ARuntime(properties, provider, executor, agentCard);
    }

}
