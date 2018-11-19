package net.consensys.eventeum.integration.broadcast.blockchain;

import lombok.AllArgsConstructor;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;

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

    public interface OnBlockchainEventListener {

        void onNewBlock(BlockDetails block);

        void onContractEvent(ContractEventDetails eventDetails);
    }

}
