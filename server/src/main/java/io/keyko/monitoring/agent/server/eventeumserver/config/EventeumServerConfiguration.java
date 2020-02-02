package io.keyko.monitoring.agent.server.eventeumserver.config;

import io.keyko.monitoring.agent.core.annotation.ConditionalOnKafkaRequired;
import net.consensys.kafkadl.annotation.EnableKafkaDeadLetter;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableKafkaDeadLetter(topics = {"#{eventeumKafkaSettings.eventeumEventsTopic}"},
                       containerFactoryBeans = {"kafkaListenerContainerFactory", "eventeumKafkaListenerContainerFactory"},
                       serviceId = "eventeum")
@ConditionalOnKafkaRequired
public class EventeumServerConfiguration {
}
