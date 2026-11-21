package io.a2aspring.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.a2aspring.autoconfigure.A2AAutoConfiguration;
import io.a2aspring.security.autoconfigure.A2ASpringSecurityAutoConfiguration;
import io.a2aspring.web.A2AWebAutoConfiguration;
import io.a2aspring.web.ServerCallContextFactory;
import io.a2aspring.web.ServerCallContextUserResolver;
import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.server.agentexecution.RequestContext;
import org.a2aproject.sdk.server.auth.TaskAuthorizationProvider;
import org.a2aproject.sdk.server.requesthandlers.RequestHandler;
import org.a2aproject.sdk.server.tasks.AgentEmitter;
import org.a2aproject.sdk.spec.AgentCard;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class A2ASpringSecurityAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    JacksonAutoConfiguration.class,
                    A2AAutoConfiguration.class,
                    A2ASpringSecurityAutoConfiguration.class,
                    A2AWebAutoConfiguration.class))
            .withUserConfiguration(TestAgentConfiguration.class);

    @Test
    void requiredTaskAuthorizationFailsWithoutApplicationProvider() {
        contextRunner.withPropertyValues("a2a.security.enabled=true").run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                    .hasRootCauseMessage("a2a.security.enabled=true requires an application-provided "
                            + "TaskAuthorizationProvider bean");
        });
    }

    @Test
    void providerCanBeSuppliedAndSecurityMetadataRemainsApplicationOwned() {
        contextRunner.withUserConfiguration(ProviderConfiguration.class)
                .withPropertyValues("a2a.security.enabled=true")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(TaskAuthorizationProvider.class);
                    assertThat(context).hasSingleBean(RequestHandler.class);
                    assertThat(context).hasSingleBean(ServerCallContextFactory.class);
                    assertThat(context.getBean(ServerCallContextUserResolver.class))
                            .isInstanceOf(SpringSecurityServerCallContextUserResolver.class);

                    AgentCard card = context.getBean(AgentCard.class);
                    assertThat(card.securitySchemes()).isNullOrEmpty();
                    assertThat(card.securityRequirements()).isNullOrEmpty();
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class TestAgentConfiguration {
        @Bean
        AgentExecutor testAgentExecutor() {
            return (RequestContext context, AgentEmitter emitter) -> { };
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class ProviderConfiguration {
        @Bean
        TaskAuthorizationProvider taskAuthorizationProvider() {
            return new TaskAuthorizationProvider() {
                @Override
                public boolean checkRead(org.a2aproject.sdk.server.ServerCallContext context,
                                         String taskId,
                                         org.a2aproject.sdk.server.auth.TaskOperation operation) {
                    return true;
                }

                @Override
                public boolean checkWrite(org.a2aproject.sdk.server.ServerCallContext context,
                                          String taskId,
                                          org.a2aproject.sdk.server.auth.TaskOperation operation) {
                    return true;
                }

                @Override
                public boolean checkCreate(org.a2aproject.sdk.server.ServerCallContext context,
                                           org.a2aproject.sdk.server.auth.TaskOperation operation) {
                    return true;
                }

                @Override
                public boolean isTaskRecorded(String taskId) {
                    return false;
                }

                @Override
                public void recordOwnership(org.a2aproject.sdk.server.ServerCallContext context,
                                            String taskId,
                                            org.a2aproject.sdk.server.auth.TaskOperation operation) {
                }
            };
        }
    }
}
