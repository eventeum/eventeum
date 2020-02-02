package io.keyko.monitoring.agent.core.chain.block;

import io.keyko.monitoring.agent.core.chain.factory.BlockDetailsFactory;
import io.keyko.monitoring.agent.core.chain.service.domain.Block;
import io.keyko.monitoring.agent.core.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import lombok.AllArgsConstructor;
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
