package io.a2aspring.security;

import java.security.Principal;
import java.util.Objects;
import jakarta.servlet.http.HttpServletRequest;
import io.a2aspring.web.ServerCallContextUserResolver;
import org.a2aproject.sdk.server.auth.UnauthenticatedUser;
import org.a2aproject.sdk.server.auth.User;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

/** Resolves Spring Security's request principal without copying credentials or claims. */
public final class SpringSecurityServerCallContextUserResolver implements ServerCallContextUserResolver {
    private final AuthenticationTrustResolver trustResolver;
    private final SpringSecurityUserMapper userMapper;

    public SpringSecurityServerCallContextUserResolver(SpringSecurityUserMapper userMapper) {
        this(new AuthenticationTrustResolverImpl(), userMapper);
    }

    SpringSecurityServerCallContextUserResolver(
            AuthenticationTrustResolver trustResolver, SpringSecurityUserMapper userMapper) {
        this.trustResolver = Objects.requireNonNull(trustResolver, "trustResolver");
        this.userMapper = Objects.requireNonNull(userMapper, "userMapper");
    }

    @Override
    public User resolve(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (!(principal instanceof Authentication authentication)
                || !trustResolver.isAuthenticated(authentication)) {
            return UnauthenticatedUser.INSTANCE;
        }
        return Objects.requireNonNull(userMapper.map(authentication),
                "SpringSecurityUserMapper must return an AuthenticatedUser");
    }
}
