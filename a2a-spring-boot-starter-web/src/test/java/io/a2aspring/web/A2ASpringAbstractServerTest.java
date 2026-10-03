package io.a2aspring.web;

import org.a2aproject.sdk.client.ClientBuilder;
import org.a2aproject.sdk.client.http.JdkA2AHttpClient;
import org.a2aproject.sdk.client.transport.jsonrpc.JSONRPCTransport;
import org.a2aproject.sdk.client.transport.jsonrpc.JSONRPCTransportConfigBuilder;
import org.a2aproject.sdk.server.apps.common.AbstractA2AServerTest;
import org.junit.jupiter.api.Disabled;

/**
 * Compatibility-test integration point required by the A2A Java integration
 * guide.
 *
 * <p>The upstream suite is intentionally disabled for the current MVP because
 * it requires task-store/queue test endpoints and exercises streaming, task
 * operations, and push-notification APIs that are not exposed by the current
 * Spring MVC starter. This class keeps the official test contract compiled and
 * records the exact transport wiring to enable once those capabilities land.
 */
@Disabled("Enable after the Spring adapter exposes the full AbstractA2AServerTest contract")
class A2ASpringAbstractServerTest extends AbstractA2AServerTest {
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
}
