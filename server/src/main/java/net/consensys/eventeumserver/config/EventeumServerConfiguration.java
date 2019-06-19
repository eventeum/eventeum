package net.consensys.eventeumserver.config;

import net.consensys.eventeum.annotation.ConditionalOnKafkaRequired;
import net.consensys.kafkadl.annotation.EnableKafkaDeadLetter;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableKafkaDeadLetter(topics = {"#{eventeumKafkaSettings.eventeumEventsTopic}"},
                       containerFactoryBeans = {"kafkaListenerContainerFactory", "eventeumKafkaListenerContainerFactory"},
                       serviceId = "eventeum")
@ConditionalOnKafkaRequired
public class EventeumServerConfiguration {
}
