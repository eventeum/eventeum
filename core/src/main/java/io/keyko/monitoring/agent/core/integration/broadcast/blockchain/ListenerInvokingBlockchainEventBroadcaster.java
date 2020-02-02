package io.keyko.monitoring.agent.core.integration.broadcast.blockchain;

import lombok.AllArgsConstructor;
import io.keyko.monitoring.agent.core.dto.block.BlockDetails;
import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionDetails;

@AllArgsConstructor
public class ListenerInvokingBlockchainEventBroadcaster implements BlockchainEventBroadcaster {

    private OnBlockchainEventListener listener;

    @Override
    public void broadcastNewBlock(BlockDetails block) {
        listener.onNewBlock(block);
    }

    @Override
    public void broadcastContractEvent(ContractEventDetails eventDetails) {
        listener.onContractEvent(eventDetails);
    }

    @Override
    public void broadcastTransaction(TransactionDetails transactionDetails) {
        listener.onTransactionEvent(transactionDetails);
    }

    public interface OnBlockchainEventListener {

        void onNewBlock(BlockDetails block);

        void onContractEvent(ContractEventDetails eventDetails);

        void onTransactionEvent(TransactionDetails transactionDetails);
    }

}
