package net.consensys.eventeum.integration.broadcast.blockchain;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.BlockEvent;
import net.consensys.eventeum.dto.message.ContractEvent;
import net.consensys.eventeum.dto.message.EventeumMessage;
import net.consensys.eventeum.integration.KafkaSettings;
import net.consensys.eventeum.utils.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

/**
 * A BlockchainEventBroadcaster that broadcasts the events to a Kafka queue.
 *
 * The key for each message will defined by the correlationIdStrategy if configured,
 * or a combination of the transactionHash, blockHash and logIndex otherwise.
 *
 * The topic names for block and contract events can be configured via the
 * kafka.topic.contractEvents and kafka.topic.blockEvents properties.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class KafkaBlockchainEventBroadcaster implements BlockchainEventBroadcaster {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaBlockchainEventBroadcaster.class);

    private KafkaTemplate<String, EventeumMessage> kafkaTemplate;

    private KafkaSettings kafkaSettings;

    private CrudRepository<ContractEventFilter, String> filterRespository;

    public KafkaBlockchainEventBroadcaster(KafkaTemplate<String, EventeumMessage> kafkaTemplate,
                                    KafkaSettings kafkaSettings,
                                    CrudRepository<ContractEventFilter, String> filterRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaSettings = kafkaSettings;
        this.filterRespository = filterRepository;
    }

    @Override
    public void broadcastNewBlock(BlockDetails block) {
        final EventeumMessage<BlockDetails> message = createBlockEventMessage(block);
        LOG.info("Sending block message: " + JSON.stringify(message));

        kafkaTemplate.send(kafkaSettings.getBlockEventsTopic(), message.getId(), message);
    }

    @Override
    public void broadcastContractEvent(ContractEventDetails eventDetails) {
        final EventeumMessage<ContractEventDetails> message = createContractEventMessage(eventDetails);
        LOG.info("Sending contract event message: " + JSON.stringify(message));

        kafkaTemplate.send(kafkaSettings.getContractEventsTopic(), getContractEventCorrelationId(message), message);
    }

    protected EventeumMessage<BlockDetails> createBlockEventMessage(BlockDetails blockDetails) {
        return new BlockEvent(blockDetails);
    }

    protected EventeumMessage<ContractEventDetails> createContractEventMessage(ContractEventDetails contractEventDetails) {
        return new ContractEvent(contractEventDetails);
    }

    private String getContractEventCorrelationId(EventeumMessage<ContractEventDetails> message) {
        final Optional<ContractEventFilter> filter = filterRespository.findById(message.getDetails().getFilterId());

        if (!filter.isPresent() || filter.get().getCorrelationIdStrategy() == null) {
            return message.getId();
        }

        return filter
                .get()
                .getCorrelationIdStrategy()
                .getCorrelationId(message.getDetails());
    }
}
