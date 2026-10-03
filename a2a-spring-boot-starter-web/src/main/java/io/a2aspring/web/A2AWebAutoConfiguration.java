package io.a2aspring.web;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import io.a2aspring.core.A2ARuntime;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Any;
import org.a2aproject.sdk.server.auth.TaskAuthorizationProvider;
import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.server.events.InMemoryQueueManager;
import org.a2aproject.sdk.server.events.MainEventBus;
import org.a2aproject.sdk.server.events.MainEventBusProcessor;
import org.a2aproject.sdk.server.requesthandlers.DefaultRequestHandler;
import org.a2aproject.sdk.server.requesthandlers.RequestHandler;
import org.a2aproject.sdk.server.tasks.InMemoryTaskStore;
import org.a2aproject.sdk.server.tasks.PushNotificationSender;
import org.a2aproject.sdk.server.tasks.TaskStore;
import org.a2aproject.sdk.server.multitenancy.AgentExecutorRouter;
import org.a2aproject.sdk.transport.jsonrpc.handler.JSONRPCHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(JSONRPCHandler.class)
@ConditionalOnBean(A2ARuntime.class)
public class A2AWebAutoConfiguration {

    @Bean
    @Any
    @ConditionalOnMissingBean(name = "a2aAuthorizationProviderInstance")
    Instance<TaskAuthorizationProvider> a2aAuthorizationProviderInstance() {
        return SpringCdiInstance.empty();
    }

    @Bean
    @Any
    @ConditionalOnMissingBean(name = "a2aAgentExecutorRouterInstance")
    Instance<AgentExecutorRouter> a2aAgentExecutorRouterInstance() {
        return SpringCdiInstance.empty();
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(name = "a2aExecutor")
    ExecutorService a2aExecutor() {
        return Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "a2a-spring-worker");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Bean
    @ConditionalOnMissingBean
    InMemoryTaskStore a2aTaskStore() {
        return new InMemoryTaskStore();
    }

    @Bean
    @ConditionalOnMissingBean
    PushNotificationSender a2aPushNotificationSender() {
        return (eventKind, task) -> {
            // Push notification delivery is intentionally disabled until a Spring-configurable
            // PushNotificationSender is provided by the application.
        };
    }

    @Bean
    @ConditionalOnMissingBean
    MainEventBus a2aMainEventBus() {
        return new MainEventBus();
    }

    @Bean
    @ConditionalOnMissingBean
    InMemoryQueueManager a2aQueueManager(InMemoryTaskStore taskStore, MainEventBus eventBus) {
        return new InMemoryQueueManager(taskStore, eventBus);
    }

    @Bean
    @ConditionalOnMissingBean
    MainEventBusProcessor a2aEventBusProcessor(MainEventBus eventBus, InMemoryTaskStore taskStore,
                                                PushNotificationSender pushNotificationSender,
                                                InMemoryQueueManager queueManager) {
        MainEventBusProcessor processor = new MainEventBusProcessor(eventBus, taskStore,
                pushNotificationSender, queueManager);
        processor.start();
        return processor;
    }

    @Bean
    @ConditionalOnMissingBean
    RequestHandler a2aRequestHandler(AgentExecutor executor, TaskStore taskStore,
                                     InMemoryQueueManager queueManager,
                                     MainEventBusProcessor processor,
                                     ExecutorService a2aExecutor) {
        return DefaultRequestHandler.builder()
                .agentExecutor(executor)
                .taskStore(taskStore)
                .queueManager(queueManager)
                .mainEventBusProcessor(processor)
                .executor(a2aExecutor)
                .eventConsumerExecutor(a2aExecutor)
                .pushNotificationsEnabled(false)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    JSONRPCHandler a2aJsonRpcHandler(A2ARuntime runtime, RequestHandler requestHandler,
                                     ExecutorService a2aExecutor) {
        return new JSONRPCHandler(runtime.agentCard(), requestHandler, a2aExecutor);
    }

    @Bean
    @ConditionalOnMissingBean
    ServerCallContextFactory a2aServerCallContextFactory() {
        return new ServletServerCallContextFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    A2AWebController a2aWebController(JSONRPCHandler handler,
                                      ServerCallContextFactory contextFactory) {
        return new A2AWebController(handler, contextFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    A2AAgentCardController a2aAgentCardController(A2ARuntime runtime) {
        return new A2AAgentCardController(runtime);
    }
}
