package io.a2aspring.security;

import org.a2aproject.sdk.server.auth.AuthenticatedUser;
import org.springframework.security.core.Authentication;

/** Maps an authenticated Spring Security identity to the SDK user model. */
@FunctionalInterface
public interface SpringSecurityUserMapper {
    AuthenticatedUser map(Authentication authentication);
}
