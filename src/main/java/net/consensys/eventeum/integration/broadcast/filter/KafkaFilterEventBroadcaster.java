package net.consensys.eventeum.integration.broadcast.filter;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.ContractEventFilterAdded;
import net.consensys.eventeum.dto.message.ContractEventFilterRemoved;
import net.consensys.eventeum.dto.message.Message;
import net.consensys.eventeum.integration.KafkaSettings;
import net.consensys.eventeum.utils.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * A FilterEventBroadcaster that broadcasts the events to a Kafka queue.
 *
 * The topic name can be configured via the kafka.topic.filterEvents property.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class KafkaFilterEventBroadcaster implements FilterEventBroadcaster {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaFilterEventBroadcaster.class);

    private KafkaTemplate<String, Message> kafkaTemplate;

    private KafkaSettings kafkaSettings;

    public KafkaFilterEventBroadcaster(KafkaTemplate<String, Message> kafkaTemplate,
                                       KafkaSettings kafkaSettings) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaSettings = kafkaSettings;
    }

    @Override
    public void broadcastEventFilterAdded(ContractEventFilter filter) {
        sendMessage(createContractEventFilterAddedMessage(filter));
    }

    @Override
    public void broadcastEventFilterRemoved(ContractEventFilter filter) {
        sendMessage(createContractEventFilterRemovedMessage(filter));
    }

    protected Message createContractEventFilterAddedMessage(ContractEventFilter filter) {
        return new ContractEventFilterAdded(filter);
    }

    protected Message createContractEventFilterRemovedMessage(ContractEventFilter filter) {
        return new ContractEventFilterRemoved(filter);
    }

    private void sendMessage(Message message) {
        LOG.info("Sending message: " + JSON.stringify(message));
        kafkaTemplate.send(kafkaSettings.getFilterEventsTopic(), message.getId(), message);
    }
}
