package io.keyko.monitoring.agent.core.integration.consumer;

import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import io.keyko.monitoring.agent.core.dto.message.ContractEventFilterAdded;
import io.keyko.monitoring.agent.core.dto.message.ContractEventFilterRemoved;
import io.keyko.monitoring.agent.core.dto.message.TransactionMonitorAdded;
import io.keyko.monitoring.agent.core.dto.message.TransactionMonitorRemoved;
import io.keyko.monitoring.agent.core.integration.KafkaSettings;
import io.keyko.monitoring.agent.core.model.TransactionMonitoringSpec;
import io.keyko.monitoring.agent.core.service.SubscriptionService;
import io.keyko.monitoring.agent.core.service.TransactionMonitoringService;
import io.keyko.monitoring.agent.core.service.exception.NotFoundException;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A FilterEventConsumer that consumes ContractFilterEvents messages from a Kafka topic.
 * <p>
 * The topic to be consumed from can be configured via the kafka.topic.contractEvents property.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class KafkaFilterEventConsumer implements EventeumInternalEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaFilterEventConsumer.class);

    private final Map<String, Consumer<GenericRecord>> messageConsumers;

    @Autowired
    public KafkaFilterEventConsumer(SubscriptionService subscriptionService,
                                    TransactionMonitoringService transactionMonitoringService,
                                    KafkaSettings kafkaSettings) {

        messageConsumers = new HashMap<>();
        messageConsumers.put(ContractEventFilterAdded.TYPE, (message) -> {
            subscriptionService.registerContractEventFilter(
                    (ContractEventFilter) message.get("details"), false);
        });

        messageConsumers.put(ContractEventFilterRemoved.TYPE, (message) -> {
            try {
                subscriptionService.unregisterContractEventFilter(
                        ((ContractEventFilter) message.get("details")).getId(), false);
            } catch (NotFoundException e) {
                logger.debug("Received filter removed message but filter doesn't exist. (We probably sent message)");
            }
        });

        messageConsumers.put(TransactionMonitorAdded.TYPE, (message) -> {
            transactionMonitoringService.registerTransactionsToMonitor(
                    (TransactionMonitoringSpec) message.get("details"), false);
        });

        messageConsumers.put(TransactionMonitorRemoved.TYPE, (message) -> {
            try {
                transactionMonitoringService.stopMonitoringTransactions(
                        ((TransactionMonitoringSpec) message.get("details")).getId(), false);
            } catch (NotFoundException e) {
                logger.debug("Received transaction monitor removed message but monitor doesn't exist. (We probably sent message)");
            }
        });
    }

    @Override
    @KafkaListener(topics = "#{eventeumKafkaSettings.eventeumEventsTopic}", groupId = "#{eventeumKafkaSettings.groupId}",
            containerFactory = "eventeumKafkaListenerContainerFactory")
    public void onMessage(GenericRecord message) {
        final Consumer<GenericRecord> consumer = messageConsumers.get(message.get("type"));

        if (consumer == null) {
            logger.error(String.format("No consumer for message type %s!", message.get("type")));
            return;
        }

        consumer.accept(message);
    }
}
