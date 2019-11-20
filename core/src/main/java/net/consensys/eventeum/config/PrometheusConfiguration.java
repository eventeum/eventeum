package net.consensys.eventeum.config;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class PrometheusConfiguration {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(Environment environment) {
        return registry -> registry.config().commonTags("application", "Eventeum", "environment",environment.getActiveProfiles()[0]);
    }

    @Bean
    public PrometheusMeterRegistry.Config configurePrometheus(MeterRegistry meterRegistry) {
        return meterRegistry.config().namingConvention(new CustomNamingConvention());
    }

}
