package net.consensys.eventeum.chain.contract;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A contract event listener that broadcasts the event details via the configured broadcaster.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
public class BroadcastingEventListener implements ContractEventListener {

    private BlockchainEventBroadcaster eventBroadcaster;

    @Autowired
    public BroadcastingEventListener(BlockchainEventBroadcaster eventBroadcaster) {
        this.eventBroadcaster = eventBroadcaster;
    }

    @Override
    public void onEvent(ContractEventDetails eventDetails) {
        eventBroadcaster.broadcastContractEvent(eventDetails);
    }
}
