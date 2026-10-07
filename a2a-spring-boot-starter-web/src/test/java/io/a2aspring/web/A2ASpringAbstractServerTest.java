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
 * <p>The official suite has been exercised against a real Spring Boot server
 * during conformance work, but remains disabled until the Spring adapter
 * supplies the suite's test-only store/queue hooks, request-scoped context
 * propagation, extended-card behavior, and all streaming/error semantics.
 */
@Disabled("Enable after the remaining AbstractA2AServerTest contract is implemented")
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
