package io.a2aspring.security.autoconfigure;

import io.a2aspring.security.SpringSecurityServerCallContextUserResolver;
import io.a2aspring.security.SpringSecurityUserMapper;
import io.a2aspring.web.A2AWebAutoConfiguration;
import io.a2aspring.web.ServerCallContextUserResolver;
import org.a2aproject.sdk.server.auth.AuthenticatedUser;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;

/** Optional Spring Security adapter; it does not install endpoint security rules. */
@AutoConfiguration(before = A2AWebAutoConfiguration.class)
@ConditionalOnClass(Authentication.class)
public class A2ASpringSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    SpringSecurityUserMapper a2aSpringSecurityUserMapper() {
        return authentication -> new AuthenticatedUser(authentication.getName());
    }

    @Bean
    @ConditionalOnMissingBean(ServerCallContextUserResolver.class)
    ServerCallContextUserResolver a2aSpringSecurityUserResolver(SpringSecurityUserMapper userMapper) {
        return new SpringSecurityServerCallContextUserResolver(userMapper);
    }
}
