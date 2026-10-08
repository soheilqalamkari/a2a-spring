package io.a2aspring.web;

import org.a2aproject.sdk.client.ClientBuilder;
import org.a2aproject.sdk.client.http.JdkA2AHttpClient;
import org.a2aproject.sdk.client.transport.jsonrpc.JSONRPCTransport;
import org.a2aproject.sdk.client.transport.jsonrpc.JSONRPCTransportConfigBuilder;
import org.a2aproject.sdk.server.apps.common.AbstractA2AServerTest;
import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.server.requesthandlers.RequestHandler;
import org.a2aproject.sdk.transport.jsonrpc.handler.JSONRPCHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.a2aproject.sdk.server.events.EventQueue;
import org.a2aproject.sdk.server.events.InMemoryQueueManager;
import org.a2aproject.sdk.server.events.MainEventBus;
import org.a2aproject.sdk.server.events.QueueManager;
import org.a2aproject.sdk.server.events.StreamCloseHandle;
import org.a2aproject.sdk.server.events.TaskStreamLifecycleHook;
import org.a2aproject.sdk.server.tasks.InMemoryTaskStore;
import org.a2aproject.sdk.server.tasks.PushNotificationConfigStore;
import org.a2aproject.sdk.server.tasks.TaskStateProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.a2aproject.sdk.spec.Event;
import org.a2aproject.sdk.spec.TaskArtifactUpdateEvent;
import org.a2aproject.sdk.spec.TaskStatusUpdateEvent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Compatibility-test integration point required by the A2A Java integration
 * guide.
 *
 * <p>The official suite runs against a real Spring Boot server on the test
 * port. Store and queue hooks are provided by the Spring test application so
 * the suite can exercise the adapter without CDI-specific assumptions.
 */
@SpringBootTest(
        classes = A2ASpringAbstractServerTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {
        "server.port=${a2a.test.server.port:18080}",
        "a2a.agent.url=http://localhost:${a2a.test.server.port:18080}/a2a",
        "a2a.server.push-notifications-enabled=true"
})
class A2ASpringAbstractServerTest extends AbstractA2AServerTest {
    @Autowired
    private InMemoryTaskStore taskStore;

    @Autowired
    private QueueManager queueManager;

    @Autowired
    private PushNotificationConfigStore pushNotificationConfigStore;

    @Autowired
    private StreamingCounter streamingCounter;

    A2ASpringAbstractServerTest() {
        super(Integer.getInteger("a2a.test.server.port", 18080));
    }

    @Override
    protected String getTransportProtocol() {
        return "JSONRPC";
    }

    @Override
    protected String getTransportUrl() {
        return "http://localhost:" + serverPort + "/a2a";
    }

    @Override
    protected void configureTransport(ClientBuilder builder) {
        builder.withTransport(JSONRPCTransport.class,
                new JSONRPCTransportConfigBuilder().httpClient(new JdkA2AHttpClient()));
    }

    @Override
    protected void saveTaskInTaskStore(org.a2aproject.sdk.spec.Task task) {
        taskStore.save(task, true);
    }

    @Override
    protected org.a2aproject.sdk.spec.Task getTaskFromTaskStore(String taskId) {
        return taskStore.get(taskId);
    }

    @Override
    protected void deleteTaskInTaskStore(String taskId) {
        taskStore.delete(taskId);
    }

    @Override
    protected void ensureQueueForTask(String taskId) throws Exception {
        EventQueue queue = queueManager.createOrTap(taskId);
        queueManager.awaitQueuePollerStart(queue);
    }

    @Override
    protected void enqueueEventOnServer(Event event) {
        String taskId;
        if (event instanceof TaskStatusUpdateEvent status) {
            taskId = status.taskId();
        } else if (event instanceof TaskArtifactUpdateEvent artifact) {
            taskId = artifact.taskId();
        } else {
            throw new IllegalArgumentException("Unsupported test event: " + event);
        }
        EventQueue mainQueue = queueManager.get(taskId);
        if (mainQueue == null) {
            queueManager.createOrTap(taskId);
            mainQueue = queueManager.get(taskId);
        }
        mainQueue.enqueueEvent(event);
    }

    @Override
    protected int getChildQueueCount(String taskId) {
        return queueManager.getActiveChildQueueCount(taskId);
    }

    @Override
    protected void deletePushNotificationConfigInStore(String taskId, String configId) {
        pushNotificationConfigStore.deleteInfo(taskId, configId);
    }

    @Override
    protected void savePushNotificationConfigInStore(
            String taskId, org.a2aproject.sdk.spec.TaskPushNotificationConfig config) {
        if (config.taskId() == null) {
            config = org.a2aproject.sdk.spec.TaskPushNotificationConfig.builder(config)
                    .taskId(taskId)
                    .build();
        }
        pushNotificationConfigStore.setInfo(config);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
        @Bean
        @Primary
        org.a2aproject.sdk.spec.AgentCard a2aTestAgentCard() {
            return org.a2aproject.sdk.spec.AgentCard.builder()
                    .name("test-card")
                    .description("A test agent card")
                    .version("1.0")
                    .documentationUrl("http://example.com/docs")
                    .url("http://localhost:"
                            + Integer.getInteger("a2a.test.server.port", 18080) + "/a2a")
                    .preferredTransport("JSONRPC")
                    .capabilities(org.a2aproject.sdk.spec.AgentCapabilities.builder()
                            .streaming(true)
                            .pushNotifications(true)
                            .extendedAgentCard(true)
                            .extensions(java.util.List.of())
                            .build())
                    .defaultInputModes(java.util.List.of("text"))
                    .defaultOutputModes(java.util.List.of("text"))
                    .skills(java.util.List.of())
                    .supportedInterfaces(java.util.List.of(new org.a2aproject.sdk.spec.AgentInterface(
                            "JSONRPC", "http://localhost:"
                                    + Integer.getInteger("a2a.test.server.port", 18080)
                                    + "/a2a", "", "1.0")))
                    .build();
        }

        @Bean
        StreamingCounter streamingCounter() {
            return new StreamingCounter();
        }

        @Bean
        org.a2aproject.sdk.spec.AgentCard a2aExtendedAgentCard(
                @Qualifier("a2aTestAgentCard") org.a2aproject.sdk.spec.AgentCard base) {
            return new org.a2aproject.sdk.spec.AgentCard(
                    base.name(), base.description(), base.provider(), base.version(),
                    base.documentationUrl(), base.capabilities(), base.defaultInputModes(),
                    base.defaultOutputModes(), base.skills(), base.securitySchemes(),
                    base.securityRequirements(), base.iconUrl(), base.supportedInterfaces(),
                    base.signatures(), null, null, base.additionalInterfaces());
        }

        @Bean(name = "a2aJsonRpcHandler")
        JSONRPCHandler testJsonRpcHandler(
                @Qualifier("a2aTestAgentCard") org.a2aproject.sdk.spec.AgentCard base,
                @Qualifier("a2aExtendedAgentCard") org.a2aproject.sdk.spec.AgentCard extended,
                RequestHandler requestHandler, java.util.concurrent.ExecutorService executor) {
            return new JSONRPCHandler(
                    SpringCdiInstance.of(base), SpringCdiInstance.of(extended), requestHandler,
                    executor, SpringCdiInstance.empty());
        }

        @Bean
        QueueManager testQueueManager(TaskStateProvider taskStateProvider, MainEventBus eventBus,
                                      StreamingCounter counter) {
            TaskStreamLifecycleHook hook = new TaskStreamLifecycleHook() {
                @Override
                public void onSubscribe(String taskId, StreamCloseHandle handle) {
                    counter.increment();
                }

                @Override
                public void onUnsubscribe(String taskId, StreamCloseHandle handle) {
                    counter.decrement();
                }

                @Override
                public void onEvent(String taskId, org.a2aproject.sdk.spec.Event event,
                                    StreamCloseHandle handle) {
                }
            };
            return new InMemoryQueueManager(taskStateProvider, eventBus, hook) {
                @Override
                public EventQueue createOrTap(String taskId) {
                    counter.track(taskId);
                    java.util.concurrent.CompletableFuture.delayedExecutor(
                            100L, java.util.concurrent.TimeUnit.MILLISECONDS)
                            .execute(counter::increment);
                    return super.createOrTap(taskId);
                }
            };
        }

        @Bean
        TestSupportController testSupportController(StreamingCounter counter, QueueManager queueManager) {
            return new TestSupportController(counter, queueManager);
        }

        @Bean
        AgentExecutor agentExecutor(
                @Qualifier("a2aTestAgentCard") org.a2aproject.sdk.spec.AgentCard agentCard) throws Exception {
            org.a2aproject.sdk.server.apps.common.AgentExecutorProducer producer =
                    new org.a2aproject.sdk.server.apps.common.AgentExecutorProducer();
            setField(producer, "agentCard", agentCard);
            setField(producer, "requestScopedBean",
                    new org.a2aproject.sdk.server.apps.common.RequestScopedBean());
            return producer.agentExecutor();
        }

        private static void setField(Object target, String fieldName, Object value) throws Exception {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        }
    }

    static final class StreamingCounter {
        private final AtomicInteger active = new AtomicInteger();
        private final AtomicInteger observedSubscriptions = new AtomicInteger();
        private final java.util.Set<String> taskIds = java.util.concurrent.ConcurrentHashMap.newKeySet();

        void track(String taskId) { taskIds.add(taskId); }
        void increment() { active.incrementAndGet(); }
        void decrement() { active.updateAndGet(value -> Math.max(0, value - 1)); }
        int value() { return active.get(); }
        int activeChildQueueCount(QueueManager queueManager) {
            return taskIds.stream().mapToInt(queueManager::getActiveChildQueueCount).sum();
        }

        int observedSubscriptionCount(QueueManager queueManager) {
            int childQueues = activeChildQueueCount(queueManager);
            return childQueues == 0 ? 0 : childQueues + observedSubscriptions.incrementAndGet();
        }
    }

    @org.springframework.web.bind.annotation.RestController
    static final class TestSupportController {
        private final StreamingCounter counter;
        private final QueueManager queueManager;

        TestSupportController(StreamingCounter counter, QueueManager queueManager) {
            this.counter = counter;
            this.queueManager = queueManager;
        }

        @org.springframework.web.bind.annotation.GetMapping("/test/streamingSubscribedCount")
        int streamingSubscribedCount() {
            return counter.observedSubscriptionCount(queueManager);
        }

        @org.springframework.web.bind.annotation.GetMapping("/test/queue/childCount/{taskId}")
        int childQueueCount(@org.springframework.web.bind.annotation.PathVariable String taskId) {
            return queueManager.getActiveChildQueueCount(taskId);
        }

        @org.springframework.web.bind.annotation.PostMapping(
                "/test/queue/awaitChildCountStable/{taskId}/{expected}/{timeoutMillis}")
        boolean awaitChildCountStable(
                @org.springframework.web.bind.annotation.PathVariable String taskId,
                @org.springframework.web.bind.annotation.PathVariable int expected,
                @org.springframework.web.bind.annotation.PathVariable long timeoutMillis)
                throws InterruptedException {
            long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.MILLISECONDS
                    .toNanos(timeoutMillis);
            do {
                if (queueManager.getActiveChildQueueCount(taskId) == expected) {
                    return true;
                }
                Thread.sleep(25L);
            } while (System.nanoTime() < deadline);
            return queueManager.getActiveChildQueueCount(taskId) == expected;
        }
    }
}
