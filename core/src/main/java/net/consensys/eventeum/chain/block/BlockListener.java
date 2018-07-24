package net.consensys.eventeum.chain.block;

import net.consensys.eventeum.dto.block.BlockDetails;

/**
 * A listener for new block events.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface BlockListener {

    /**
     * Called when a new block is detected fron the ethereum node.
     *
     * @param blockDetails The details of the new block.
     */
    void onBlock(BlockDetails blockDetails);
}
