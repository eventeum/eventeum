package net.consensys.eventeum.integration.broadcast.blockchain;

import net.consensys.eventeum.BlockDetails;
import net.consensys.eventeum.TransactionDetails;
import net.consensys.eventeum.ContractEventDetails;

/**
 * An interface for a class that broadcasts ethereum blockchain details to the wider system.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface BlockchainEventBroadcaster {

    /**
     * Broadcast details of a new block that has been mined.
     *
     * @param block
     */
    void broadcastNewBlock(BlockDetails block);

    /**
     * Broadcasts details of a new smart contract event that has been emitted from the ethereum blockchain.
     * @param eventDetails
     */
    void broadcastContractEvent(ContractEventDetails eventDetails);

    /**
     * Broadcasts details of a monitored transaction that has been mined.
     * @param transactionDetails
     */
    void broadcastTransaction(TransactionDetails transactionDetails);
}
