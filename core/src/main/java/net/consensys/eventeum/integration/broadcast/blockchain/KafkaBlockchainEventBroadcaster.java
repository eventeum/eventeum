package net.consensys.eventeum.integration.broadcast.blockchain;

import net.consensys.eventeum.*;
import net.consensys.eventeum.chain.service.domain.Transaction;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.EventeumMessage;
import net.consensys.eventeum.integration.KafkaSettings;
import net.consensys.eventeum.utils.JSON;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

/**
 * A BlockchainEventBroadcaster that broadcasts the events to a Kafka queue.
 * <p>
 * The key for each message will defined by the correlationIdStrategy if configured,
 * or a combination of the transactionHash, blockHash and logIndex otherwise.
 * <p>
 * The topic names for block and contract events can be configured via the
 * kafka.topic.contractEvents and kafka.topic.blockEvents properties.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class KafkaBlockchainEventBroadcaster implements BlockchainEventBroadcaster {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaBlockchainEventBroadcaster.class);

    private KafkaTemplate<String, GenericRecord> kafkaTemplate;

    private KafkaSettings kafkaSettings;

    private CrudRepository<ContractEventFilter, String> filterRespository;

    public KafkaBlockchainEventBroadcaster(KafkaTemplate<String, GenericRecord> kafkaTemplate,
                                           KafkaSettings kafkaSettings,
                                           CrudRepository<ContractEventFilter, String> filterRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaSettings = kafkaSettings;
        this.filterRespository = filterRepository;
    }

    @Override
    public void broadcastNewBlock(BlockDetails block) {
        final BlockEvent message = createBlockEventMessage(block);
        LOG.info("Sending block message: " + JSON.stringify(message));

        GenericRecord genericRecord = new GenericData.Record(BlockEvent.getClassSchema());
        genericRecord.put("id", message.getId());
        genericRecord.put("type", message.getType());
        genericRecord.put("details", message.getDetails());
        genericRecord.put("retries", message.getRetries());


        kafkaTemplate.send(kafkaSettings.getBlockEventsTopic(), message.getId(), genericRecord);
    }

    @Override
    public void broadcastContractEvent(ContractEventDetails eventDetails) {
        final ContractEvent message = createContractEventMessage(eventDetails);
        LOG.info("Sending contract event message: " + JSON.stringify(message));
        GenericRecord genericRecord = new GenericData.Record(ContractEvent.getClassSchema());
        genericRecord.put("id", message.getId());
        genericRecord.put("type", message.getType());
        genericRecord.put("details", message.getDetails());
        genericRecord.put("retries", message.getRetries());

        kafkaTemplate.send(kafkaSettings.getContractEventsTopic(), getContractEventCorrelationId(message), genericRecord);
    }

    @Override
    public void broadcastTransaction(TransactionDetails transactionDetails) {
        final TransactionEvent message = createTransactionEventMessage(transactionDetails);
        LOG.info("Sending transaction event message: " + JSON.stringify(message));
        GenericRecord genericRecord = new GenericData.Record(TransactionEvent.getClassSchema());
        genericRecord.put("id", message.getId());
        genericRecord.put("type", message.getType());
        genericRecord.put("details", message.getDetails());
        genericRecord.put("retries", message.getRetries());
        kafkaTemplate.send(kafkaSettings.getTransactionEventsTopic(), transactionDetails.getBlockHash(), genericRecord);
    }

    protected BlockEvent createBlockEventMessage(BlockDetails blockDetails) {
        return new BlockEvent(blockDetails.getHash(), "CONTRACT_EVENT", blockDetails, 0);
    }

    protected ContractEvent createContractEventMessage(ContractEventDetails contractEventDetails) {
        return new ContractEvent(contractEventDetails.getId(), "CONTRACT_EVENT", contractEventDetails, 0);
    }

    protected TransactionEvent createTransactionEventMessage(TransactionDetails transactionDetails) {
        return new TransactionEvent(transactionDetails.getHash(), "TRANSACTION", transactionDetails, 0);
    }

    private String getContractEventCorrelationId(ContractEvent message) {
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
