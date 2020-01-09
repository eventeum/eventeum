package net.consensys.eventeum.chain.block;

import lombok.AllArgsConstructor;
import net.consensys.eventeum.chain.factory.BlockDetailsFactory;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import org.springframework.stereotype.Component;

/**
 * A block listener that broadcasts the block details via the configured broadcaster.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
@AllArgsConstructor
public class BroadcastingBlockListener implements BlockListener {

    private BlockchainEventBroadcaster eventBroadcaster;

    private BlockDetailsFactory blockDetailsFactory;

    @Override
    public void onBlock(Block block) {
        eventBroadcaster.broadcastNewBlock(blockDetailsFactory.createBlockDetails(block));
    }


}
