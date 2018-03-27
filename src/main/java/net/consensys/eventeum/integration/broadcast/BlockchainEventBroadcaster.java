package net.consensys.eventeum.integration.broadcast;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;

/**
 * An interface for a class that broadcasts etheruem blockchain details to the wider system.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface BlockchainEventBroadcaster {

    /**
     * Broadcast details of a new block that has been mined.
     * @param block
     */
    void broadcastNewBlock(BlockDetails block);

    /**
     * Broadcasts details of a new smart contract event that has been emitted from the ethereum blockchain.
     * @param eventDetails
     */
    void broadcastContractEvent(ContractEventDetails eventDetails);
}
