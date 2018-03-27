package net.consensys.eventeum.chain.block;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.integration.broadcast.BlockchainEventBroadcaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A block listener that broadcasts the block details via the configured broadcaster.
 *
 * @author Craig Williams <craig.williams@consensys.net>ß
 */
@Component
public class BroadcastingBlockListener implements BlockListener {

    private BlockchainEventBroadcaster eventBroadcaster;

    @Autowired
    public BroadcastingBlockListener(BlockchainEventBroadcaster eventBroadcaster) {
        this.eventBroadcaster = eventBroadcaster;
    }

    @Override
    public void onBlock(BlockDetails blockDetails) {
        eventBroadcaster.broadcastNewBlock(blockDetails);
    }
}
