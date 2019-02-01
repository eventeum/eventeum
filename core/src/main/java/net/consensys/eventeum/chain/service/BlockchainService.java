package net.consensys.eventeum.chain.service;

import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.contract.ContractEventListener;
import net.consensys.eventeum.chain.service.domain.TransactionReceipt;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import rx.Subscription;

import java.math.BigInteger;

/**
 * Interface for a service that interacts directly with an Ethereum blockchain node.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface BlockchainService {

    /**
     *
     * @return The ethereum node name that this service is connected to.
     */
    String getNodeName();

    /**
     * Add a listener that gets notified when a new block is mined.
     *
     * @param blockListener the listener to add
     */
    void addBlockListener(BlockListener blockListener);

    /**
     * Remove a block listener than was previously added.
     *
     * @param blockListener the listener to remove
     */
    void removeBlockListener(BlockListener blockListener);

    /**
     * Register a contract event listener for the specified event filter, that gets triggered when an event
     * matching the filter is emitted within the Ethereum network.
     *
     * @param filter The contract event filter that should be matched.
     * @param eventListener The listener to be triggered when a matching event is emitted
     * @return The registered subscriptopn
     */
    Subscription registerEventListener(ContractEventFilter filter, ContractEventListener eventListener);

    /**
     *
     * @return the client version for the connected Ethereum node.
     */
    String getClientVersion();

    /**
     *
     * @return the current block number of the network that the Ethereum node is connected to.
     */
    BigInteger getCurrentBlockNumber();

    /**
     * Obtain the transaction receipt for a specified transaction id.
     *
     * @param txId the transaction id
     * @return the receipt for the transaction with the specified id.
     */
    TransactionReceipt getTransactionReceipt(String txId);

    /**
     * Reconnects to the Ethereum node (useful after node failure)
     */
    void reconnect();

    /**
     *
     * @return true if the service is correctly connected to the ethereum node.
     */
    boolean isConnected();
}
