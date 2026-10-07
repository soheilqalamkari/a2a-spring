package io.a2aspring.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.server.agentexecution.RequestContext;
import org.a2aproject.sdk.server.tasks.AgentEmitter;
import io.a2aspring.core.A2ARuntime;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class A2AAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(A2AAutoConfiguration.class));

    @Test
    void doesNotCreateRuntimeWithoutAnExecutor() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(A2ARuntime.class));
    }

    @Test
    void discoversExecutorAndCreatesRuntime() {
        contextRunner.withUserConfiguration(ExecutorConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(A2ARuntime.class);
                    assertThat(context.getBean(A2ARuntime.class).agentCard().name())
                            .isEqualTo("spring-a2a-agent");
                });
    }

    @Test
    void serverCanBeDisabled() {
        contextRunner.withUserConfiguration(ExecutorConfiguration.class)
                .withPropertyValues("a2a.server.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(A2ARuntime.class));
    }

    @Test
    void enablesPushNotificationsOnlyWhenConfigured() {
        contextRunner.withUserConfiguration(ExecutorConfiguration.class)
                .withPropertyValues("a2a.server.push-notifications-enabled=true")
                .run(context -> assertThat(context.getBean(A2ARuntime.class).agentCard()
                        .capabilities().pushNotifications()).isTrue());
    }

    @Configuration(proxyBeanMethods = false)
    static class ExecutorConfiguration {
        @Bean
        AgentExecutor agentExecutor() {
            return (RequestContext context, AgentEmitter emitter) -> { };
        }
    }
}
