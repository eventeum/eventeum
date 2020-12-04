package net.consensys.eventeum.integration.broadcast.blockchain;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.message.BlockEvent;
import net.consensys.eventeum.dto.message.ContractEvent;
import net.consensys.eventeum.dto.message.EventeumMessage;
import net.consensys.eventeum.dto.message.TransactionEvent;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.integration.EventBridgeSettings;
import net.consensys.eventeum.utils.JSON;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;

import java.time.Instant;
import java.util.Collections;

public class EventBridgeBlockChainEventBroadcaster implements BlockchainEventBroadcaster {

    private EventBridgeClient client;

    private PutEventsRequestEntry.Builder entryBuilder;

    public EventBridgeBlockChainEventBroadcaster(EventBridgeSettings settings) {
        this.client = EventBridgeClient.builder().build();
        this.entryBuilder = PutEventsRequestEntry.builder()
                .eventBusName(settings.getEventBusName())
                .source(settings.getSource());
    }

    @Override
    public void broadcastNewBlock(BlockDetails block) {
        final EventeumMessage<BlockDetails> message = new BlockEvent(block);
        PutEventsRequestEntry entry = entryBuilder
                .time(Instant.ofEpochSecond(block.getTimestamp().longValue()))
                .detailType("BLOCK")
                .detail(JSON.stringify(message))
                .build();
        send(entry);
    }

    @Override
    public void broadcastContractEvent(ContractEventDetails eventDetails) {
        final EventeumMessage<ContractEventDetails> message = new ContractEvent(eventDetails);
        PutEventsRequestEntry entry = this.entryBuilder
                .time(Instant.ofEpochSecond(eventDetails.getTimestamp().longValue()))
                .resources(eventDetails.getAddress())
                .detailType("CONTRACT_EVENT")
                .detail(JSON.stringify(message))
                .build();
        send(entry);
    }

    @Override
    public void broadcastTransaction(TransactionDetails transactionDetails) {
        final EventeumMessage<TransactionDetails> message = new TransactionEvent(transactionDetails);
        PutEventsRequestEntry entry = this.entryBuilder
                .time(Instant.ofEpochSecond(transactionDetails.getTimestamp().longValue()))
                .resources(transactionDetails.getContractAddress())
                .detailType("TRANSACTION")
                .detail(JSON.stringify(message))
                .build();
        send(entry);
    }

    protected void send(PutEventsRequestEntry entry) {
        final PutEventsRequest request = PutEventsRequest.builder().entries(Collections.singletonList(entry)).build();
        this.client.putEvents(request);
    }
}
