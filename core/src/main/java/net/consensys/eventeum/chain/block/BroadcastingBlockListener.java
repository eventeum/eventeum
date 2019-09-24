package net.consensys.eventeum.chain.block;

import lombok.AllArgsConstructor;
import net.consensys.eventeum.chain.factory.BlockDetailsFactory;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.utils.Numeric;

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
