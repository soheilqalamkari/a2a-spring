package io.a2aspring.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.a2aproject.sdk.server.auth.AuthenticatedUser;
import org.a2aproject.sdk.server.auth.UnauthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

class SpringSecurityServerCallContextUserResolverTest {

    private final SpringSecurityServerCallContextUserResolver resolver =
            new SpringSecurityServerCallContextUserResolver(authentication ->
                    new AuthenticatedUser(authentication.getName()));

    @Test
    void mapsAuthenticatedPrincipalWithoutCopyingCredentialsOrClaims() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(UsernamePasswordAuthenticationToken.authenticated(
                "alice", "sensitive-password", AuthorityUtils.createAuthorityList("ROLE_USER")));

        var user = resolver.resolve(request);

        assertThat(user).isInstanceOf(AuthenticatedUser.class);
        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getAttribute("credentials")).isNull();
        assertThat(((AuthenticatedUser) user).attributes()).isEmpty();
    }

    @Test
    void treatsAnonymousAndUnauthenticatedSpringTokensAsUnauthenticated() {
        MockHttpServletRequest anonymousRequest = new MockHttpServletRequest();
        anonymousRequest.setUserPrincipal(new AnonymousAuthenticationToken(
                "test-key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
        MockHttpServletRequest unauthenticatedRequest = new MockHttpServletRequest();
        unauthenticatedRequest.setUserPrincipal(UsernamePasswordAuthenticationToken.unauthenticated(
                "alice", "sensitive-password"));

        assertThat(resolver.resolve(anonymousRequest)).isSameAs(UnauthenticatedUser.INSTANCE);
        assertThat(resolver.resolve(unauthenticatedRequest)).isSameAs(UnauthenticatedUser.INSTANCE);
    }

    @Test
    void treatsNonSpringServletPrincipalAsUnauthenticated() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal((java.security.Principal) () -> "container-user");

        assertThat(resolver.resolve(request)).isSameAs(UnauthenticatedUser.INSTANCE);
    }

    @Test
    void supportsAnApplicationProvidedNarrowAttributeMapper() {
        SpringSecurityServerCallContextUserResolver customResolver =
                new SpringSecurityServerCallContextUserResolver(authentication ->
                        new AuthenticatedUser(authentication.getName(), Map.of("tenant", "acme")));
        HttpServletRequest request = new MockHttpServletRequest();
        ((MockHttpServletRequest) request).setUserPrincipal(UsernamePasswordAuthenticationToken.authenticated(
                "alice", "sensitive-password", List.of()));

        var user = (AuthenticatedUser) customResolver.resolve(request);

        assertThat(user.getAttribute("tenant")).isEqualTo("acme");
        assertThat(user.getAttribute("credentials")).isNull();
    }
}
