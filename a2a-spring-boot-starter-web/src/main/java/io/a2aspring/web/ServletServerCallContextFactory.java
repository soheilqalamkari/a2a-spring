package io.a2aspring.web;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import org.a2aproject.sdk.server.ServerCallContext;
import org.a2aproject.sdk.server.auth.AuthenticatedUser;
import org.a2aproject.sdk.server.auth.UnauthenticatedUser;
import org.a2aproject.sdk.server.auth.User;
import org.a2aproject.sdk.spec.TransportProtocol;

/** Default servlet adapter for the SDK's non-null server call context. */
public final class ServletServerCallContextFactory implements ServerCallContextFactory {
    static final String HEADERS_STATE_KEY = "headers";
    private final ServerCallContextUserResolver userResolver;

    public ServletServerCallContextFactory() {
        this(ServletServerCallContextFactory::defaultUser);
    }

    public ServletServerCallContextFactory(ServerCallContextUserResolver userResolver) {
        this.userResolver = java.util.Objects.requireNonNull(userResolver, "userResolver");
    }

    @Override
    public ServerCallContext create(HttpServletRequest request) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put(ServerCallContext.TRANSPORT_KEY, TransportProtocol.JSONRPC);
        state.put(HEADERS_STATE_KEY, headers(request));

        String protocolVersion = request.getHeader("A2A-Version");
        return new ServerCallContext(userResolver.resolve(request), state, Set.of(), protocolVersion);
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

    private static User defaultUser(HttpServletRequest request) {
        var principal = request.getUserPrincipal();
        return principal == null ? UnauthenticatedUser.INSTANCE : new AuthenticatedUser(principal.getName());
    }
}
