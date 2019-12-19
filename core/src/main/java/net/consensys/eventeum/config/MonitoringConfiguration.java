package net.consensys.eventeum.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import net.consensys.eventeum.monitoring.EventeumValueMonitor;
import net.consensys.eventeum.monitoring.MicrometerValueMonitor;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class MonitoringConfiguration {

    @ConditionalOnProperty("management.endpoint.prometheus")
    public class PrometheusConfiguration {

        @Bean
        public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(Environment environment) {
            return registry -> registry.config().commonTags("application", "Eventeum", "environment",environment.getActiveProfiles()[0]);
        }

        @Bean
        public PrometheusMeterRegistry.Config configurePrometheus(MeterRegistry meterRegistry) {
            return meterRegistry.config().namingConvention(new CustomNamingConvention());
        }

        @Bean
        //Second value exists so we can guarantee that this gets called after configurePrometheus
        public EventeumValueMonitor eventeumValueMonitor(
                MeterRegistry meterRegistry, PrometheusMeterRegistry.Config config) {
            return new MicrometerValueMonitor(meterRegistry);
        }

    }

    @ConditionalOnProperty(value = "management.endpoint.prometheus", matchIfMissing = true)
    public class DoNothingMonitoringConfiguration {

        @Bean
        public EventeumValueMonitor eventeumValueMonitor() {
            return new EventeumValueMonitor() {

                @Override
                public <T extends Number> T monitor(String name, String node, T number) {
                    return number;
                }
            };
        }
    }
}
