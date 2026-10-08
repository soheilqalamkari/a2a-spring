package io.a2aspring.core;

import java.util.Optional;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Map;
import org.a2aproject.sdk.server.config.A2AConfigProvider;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/** Resolves SDK settings from Spring's complete Environment with SDK fallback support. */
public final class SpringEnvironmentA2AConfigProvider implements A2AConfigProvider {
    private final Environment environment;
    private final A2AConfigProvider defaults;
    private final Map<String, String> overrides;
    private final Properties classpathDefaults = new Properties();

    public SpringEnvironmentA2AConfigProvider(Environment environment, A2AConfigProvider defaults) {
        this(environment, defaults, Map.of());
    }

    public SpringEnvironmentA2AConfigProvider(Environment environment, A2AConfigProvider defaults, Map<String, String> overrides) {
        Assert.notNull(environment, "environment must not be null");
        Assert.notNull(defaults, "defaults must not be null");
        this.environment = environment;
        this.defaults = defaults;
        this.overrides = Map.copyOf(overrides);
        try {
            Enumeration<java.net.URL> resources = Thread.currentThread().getContextClassLoader().getResources("META-INF/a2a-defaults.properties");
            while (resources.hasMoreElements()) {
                try (InputStream stream = resources.nextElement().openStream()) {
                    classpathDefaults.load(stream);
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load A2A SDK defaults", exception);
        }
    }

    @Override
    public String getValue(String key) {
        return getOptionalValue(key).orElseThrow(() ->
                new IllegalStateException("Missing A2A configuration property: " + key));
    }

    @Override
    public Optional<String> getOptionalValue(String key) {
        return Optional.ofNullable(environment.getProperty(key))
                .or(() -> Optional.ofNullable(overrides.get(key)))
                .or(() -> defaults.getOptionalValue(key))
                .or(() -> Optional.ofNullable(classpathDefaults.getProperty(key)));
    }
}
