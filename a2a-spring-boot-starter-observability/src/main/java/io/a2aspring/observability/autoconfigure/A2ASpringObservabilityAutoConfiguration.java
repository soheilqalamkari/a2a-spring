package io.a2aspring.observability.autoconfigure;

import io.a2aspring.observability.A2ASpringMetricsRecorder;
import io.a2aspring.web.A2AWebMetricsRecorder;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/** Installs A2A metrics when this optional starter and a Micrometer registry are present. */
@AutoConfiguration
@ConditionalOnClass(MeterRegistry.class)
public class A2ASpringObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    @ConditionalOnMissingBean(A2AWebMetricsRecorder.class)
    A2AWebMetricsRecorder a2aWebMetricsRecorder(MeterRegistry meterRegistry) {
        return new A2ASpringMetricsRecorder(meterRegistry);
    }
}
