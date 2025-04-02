/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.consensys.eventeum.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import net.consensys.eventeum.monitoring.EventeumValueMonitor;
import net.consensys.eventeum.monitoring.MicrometerValueMonitor;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class MonitoringConfiguration {

  @ConditionalOnProperty(name = "management.endpoint.metrics.enabled", havingValue = "true")
  public class PrometheusConfiguration {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(Environment environment) {
      return registry ->
          registry
              .config()
              .commonTags("application", "Eventeum", "environment", getProfileName(environment));
    }

    @Bean
    public EventeumValueMonitor eventeumValueMonitor(MeterRegistry meterRegistry) {
      return new MicrometerValueMonitor(meterRegistry);
    }

    @Bean
    public PrometheusMeterRegistry.Config configurePrometheus(MeterRegistry meterRegistry) {
      return meterRegistry.config().namingConvention(new CustomNamingConvention());
    }

    private String getProfileName(Environment environment) {
      if (environment.getActiveProfiles() == null || environment.getActiveProfiles().length == 0) {
        return "Default";
      }

      return environment.getActiveProfiles()[0];
    }
  }

  @ConditionalOnProperty(
      value = "management.endpoint.metrics.enabled",
      havingValue = "false",
      matchIfMissing = true)
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
