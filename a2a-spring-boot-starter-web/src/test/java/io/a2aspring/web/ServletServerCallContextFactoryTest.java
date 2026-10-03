package io.a2aspring.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.Principal;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class ServletServerCallContextFactoryTest {
    private final ServletServerCallContextFactory factory = new ServletServerCallContextFactory();

    @Test
    void createsNonNullAnonymousUserWhenRequestIsUnauthenticated() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("A2A-Version", "1.0");

        var context = factory.create(request);

        assertThat(context.getUser()).isNotNull();
        assertThat(context.getUser().isAuthenticated()).isFalse();
        assertThat(context.getUser().getUsername()).isEqualTo("anonymous");
        assertThat(context.getRequestedProtocolVersion()).isEqualTo("1.0");
        assertThat(context.getState()).containsEntry(
                org.a2aproject.sdk.server.ServerCallContext.TRANSPORT_KEY, "jsonrpc");
        assertThat(context.getState()).containsKey(ServletServerCallContextFactory.HEADERS_STATE_KEY);
    }

    @Test
    void mapsServletPrincipalToSdkUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal((Principal) () -> "alice");
        request.addHeader("X-Request-Id", "request-1");

        var context = factory.create(request);

        assertThat(context.getUser().isAuthenticated()).isTrue();
        assertThat(context.getUser().getUsername()).isEqualTo("alice");
        @SuppressWarnings("unchecked")
        var headers = (java.util.Map<String, String>) context.getState().get("headers");
        assertThat(headers).containsEntry("X-Request-Id", "request-1");
    }
}
