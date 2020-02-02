package io.keyko.monitoring.agent.core.chain.block;

import io.keyko.monitoring.agent.core.chain.service.domain.Block;

/**
 * A listener for new block events.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface BlockListener {

    /**
     * Called when a new block is detected fron the ethereum node.
     *
     * @param block The new block
     */
    void onBlock(Block block);
}
