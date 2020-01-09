package net.consensys.eventeum.integration.broadcast.blockchain;

import lombok.AllArgsConstructor;
import net.consensys.eventeum.BlockDetails;
import net.consensys.eventeum.ContractEventDetails;
import net.consensys.eventeum.TransactionDetails;

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
