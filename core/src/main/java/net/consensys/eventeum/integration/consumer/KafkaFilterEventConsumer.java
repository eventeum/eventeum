package net.consensys.eventeum.integration.consumer;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.*;
import net.consensys.eventeum.integration.KafkaSettings;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import net.consensys.eventeum.service.TransactionMonitoringService;
import net.consensys.eventeum.service.exception.NotFoundException;
import net.consensys.eventeum.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A FilterEventConsumer that consumes ContractFilterEvents messages from a Kafka topic.
 *
 * The topic to be consumed from can be configured via the kafka.topic.contractEvents property.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class KafkaFilterEventConsumer implements EventeumInternalEventConsumer {

    private EventeumInternalEventConsumer proxiedConsumer;

    @Autowired
    public KafkaFilterEventConsumer(SubscriptionService subscriptionService,
                                    TransactionMonitoringService transactionMonitoringService,
                                    KafkaSettings kafkaSettings) {
        proxiedConsumer = new BaseEventeumEventConsumer(subscriptionService, transactionMonitoringService);
    }

    @Override
    @KafkaListener(topics = "#{eventeumKafkaSettings.eventeumEventsTopic}", groupId = "#{eventeumKafkaSettings.groupId}",
            containerFactory = "eventeumKafkaListenerContainerFactory")
    public void onMessage(EventeumMessage message) {
        proxiedConsumer.onMessage(message);
    }
}
