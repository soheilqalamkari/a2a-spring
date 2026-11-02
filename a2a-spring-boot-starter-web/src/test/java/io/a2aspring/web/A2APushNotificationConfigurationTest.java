package io.a2aspring.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.server.agentexecution.RequestContext;
import org.a2aproject.sdk.server.tasks.AgentEmitter;
import org.a2aproject.sdk.server.tasks.PushNotificationSender;
import org.a2aproject.sdk.spec.AgentCard;
import io.a2aspring.autoconfigure.A2AAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class A2APushNotificationConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(A2AAutoConfiguration.class,
                    A2AWebAutoConfiguration.class))
            .withUserConfiguration(ExecutorConfiguration.class);

    @Test
    void disabledPushNotificationsUseSafeDefaultSenderAndAreNotAdvertised() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(PushNotificationSender.class);
            assertThat(context.getBean(AgentCard.class).capabilities().pushNotifications()).isFalse();
        });
    }

    @Test
    void enablingPushNotificationsRequiresAnApplicationSender() {
        contextRunner.withPropertyValues("a2a.server.push-notifications-enabled=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining("no application PushNotificationSender bean is configured");
                });
    }

    @Test
    void applicationSenderEnablesAndAdvertisesPushNotifications() {
        contextRunner.withUserConfiguration(PushSenderConfiguration.class)
                .withPropertyValues("a2a.server.push-notifications-enabled=true")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(AgentCard.class).capabilities().pushNotifications()).isTrue();
                    assertThat(context).hasSingleBean(PushNotificationSender.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class ExecutorConfiguration {
        @Bean
        com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
            return new com.fasterxml.jackson.databind.ObjectMapper();
        }

        @Bean
        AgentExecutor agentExecutor() {
            return (RequestContext context, AgentEmitter emitter) -> { };
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class PushSenderConfiguration {
        @Bean
        PushNotificationSender pushNotificationSender() {
            return (eventKind, task) -> { };
        }
    }
}
