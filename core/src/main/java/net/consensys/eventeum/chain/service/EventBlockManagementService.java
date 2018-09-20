package net.consensys.eventeum.chain.service;

import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;

import java.math.BigInteger;

/**
 * Interface for a service that manages the latest block that has been seen to a specific event specification.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface EventBlockManagementService {

    /**
     * Update the latest block number state for an event specification.
     *
     * @param eventSpecHash The event specification hash.
     * @param blockNumber The new latest block number.
     */
    void updateLatestBlock(String eventSpecHash, BigInteger blockNumber);

    /**
     * Retrieve the block number that has to start an event specification scan.
     *
     * @param eventSpec The event specification.
     * @return The latest block number that has been seen for a specified event specification.
     */
    BigInteger getBlockNumberForScanEvent(ContractEventSpecification eventSpec);
}
