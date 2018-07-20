package net.consensys.eventeumserver.config;

import net.consensys.eventeum.annotation.ConditionalOnKafkaRequired;
import net.consensys.kafkadl.EnableKafkaDeadLetter;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableKafkaDeadLetter(topics = {"#{eventeumKafkaSettings.filterEventsTopic}"},
                       containerFactoryBeans = {"kafkaListenerContainerFactory", "eventeumKafkaListenerContainerFactory"},
                       serviceId = "eventeum")
@ConditionalOnKafkaRequired
public class EventeumServerConfiguration {
}
