package net.consensys.eventeum.integration.consumer;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.ContractEventFilterAdded;
import net.consensys.eventeum.dto.message.ContractEventFilterRemoved;
import net.consensys.eventeum.dto.message.EventeumMessage;
import net.consensys.eventeum.integration.KafkaSettings;
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
public class KafkaFilterEventConsumer implements FilterEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaFilterEventConsumer.class);

    private SubscriptionService subscriptionService;

    private KafkaSettings kafkaSettings;

    private Map<String, Consumer<EventeumMessage<ContractEventFilter>>> messageConsumers;

    @Autowired
    public KafkaFilterEventConsumer(SubscriptionService subscriptionService,
                                    KafkaSettings kafkaSettings) {
        this.subscriptionService = subscriptionService;
        this.kafkaSettings = kafkaSettings;

        messageConsumers = new HashMap<>();
        messageConsumers.put(ContractEventFilterAdded.TYPE, (message) -> {
            subscriptionService.registerContractEventFilter(message.getDetails(), false);
        });

        messageConsumers.put(ContractEventFilterRemoved.TYPE, (message) -> {
            try {
                subscriptionService.unregisterContractEventFilter(message.getDetails().getId(), false);
            } catch (NotFoundException e) {
                logger.debug("Received filter removed message but filter doesn't exist. (We probably sent message)");
            }
        });
    }

    @Override
    @KafkaListener(topics = "#{eventeumKafkaSettings.filterEventsTopic}", groupId = "#{eventeumKafkaSettings.groupId}",
            containerFactory = "eventeumKafkaListenerContainerFactory")
    public void onMessage(EventeumMessage<ContractEventFilter> message) {
        final Consumer<EventeumMessage<ContractEventFilter>> consumer = messageConsumers.get(message.getType());

        if (consumer == null) {
            logger.error(String.format("No consumer for message type %s!", message.getType()));
            return;
        }

        consumer.accept(message);
    }
}
