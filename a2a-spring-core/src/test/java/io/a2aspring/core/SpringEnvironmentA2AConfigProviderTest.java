package io.a2aspring.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.a2aproject.sdk.server.config.A2AConfigProvider;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class SpringEnvironmentA2AConfigProviderTest {
    @Test
    void springEnvironmentOverridesSdkDefaults() {
        MockEnvironment environment = new MockEnvironment().withProperty("a2a.executor.core-pool-size", "7");
        A2AConfigProvider defaults = new A2AConfigProvider() {
            @Override public String getValue(String key) { return "default"; }
            @Override public java.util.Optional<String> getOptionalValue(String key) {
                return java.util.Optional.of("default");
            }
        };

        var provider = new SpringEnvironmentA2AConfigProvider(environment, defaults);

        assertThat(provider.getValue("a2a.executor.core-pool-size")).isEqualTo("7");
        assertThat(provider.getValue("other")).isEqualTo("default");
    }
}
