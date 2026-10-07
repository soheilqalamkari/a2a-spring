package io.a2aspring.core;

import java.util.Locale;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration for the Spring integration around the official A2A Java SDK. */
@ConfigurationProperties("a2a")
public class A2AProperties {
    private final Agent agent = new Agent();
    private final Server server = new Server();
    private final Security security = new Security();

    public Agent getAgent() { return agent; }
    public Server getServer() { return server; }
    public Security getSecurity() { return security; }

    public static class Agent {
        private String name = "spring-a2a-agent";
        private String description = "A Spring Boot A2A agent";
        private String version = "0.1.0";
        private String url = "http://localhost:8080/a2a";

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    public static class Server {
        private boolean enabled = true;
        private String transport = "JSONRPC";
        private boolean pushNotificationsEnabled;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getTransport() { return transport; }
        public void setTransport(String transport) {
            this.transport = transport == null ? null : transport.toUpperCase(Locale.ROOT);
        }

        public boolean isPushNotificationsEnabled() { return pushNotificationsEnabled; }
        public void setPushNotificationsEnabled(boolean pushNotificationsEnabled) {
            this.pushNotificationsEnabled = pushNotificationsEnabled;
        }
    }

    public static class Security {
        private boolean enabled;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}
