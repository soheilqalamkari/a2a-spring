package io.a2aspring.web;

import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import org.a2aproject.sdk.server.ServerCallContext;
import org.a2aproject.sdk.server.auth.User;

/** Default servlet adapter for the SDK's non-null server call context. */
public final class ServletServerCallContextFactory implements ServerCallContextFactory {
    static final String HEADERS_STATE_KEY = "headers";

    @Override
    public ServerCallContext create(HttpServletRequest request) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put(ServerCallContext.TRANSPORT_KEY, "jsonrpc");
        state.put(HEADERS_STATE_KEY, headers(request));

        String protocolVersion = request.getHeader("A2A-Version");
        return new ServerCallContext(user(request), state, Set.of(),
                protocolVersion == null ? "1.0" : protocolVersion);
    }

    private Map<String, String> headers(HttpServletRequest request) {
        Enumeration<String> names = request.getHeaderNames();
        if (names == null) {
            return Map.of();
        }

        Map<String, String> headers = new LinkedHashMap<>();
        Collections.list(names).forEach(name -> headers.put(name, request.getHeader(name)));
        return Map.copyOf(headers);
    }

    private User user(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        return new ServletUser(principal == null ? "anonymous" : principal.getName(), principal != null);
    }

    private record ServletUser(String username, boolean authenticated) implements User {
        @Override
        public boolean isAuthenticated() {
            return authenticated;
        }

        @Override
        public String getUsername() {
            return username;
        }
    }
}
