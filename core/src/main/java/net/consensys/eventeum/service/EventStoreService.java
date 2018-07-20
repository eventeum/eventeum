package net.consensys.eventeum.service;

import net.consensys.eventeum.dto.event.ContractEventDetails;

/**
 * A service that interacts with the event store in order to retrieve data required by Eventeum.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface EventStoreService {

    /**
     * Returns the contract event with the latest block, that matches the event signature.
     *
     * @param eventSignature The event signature
     * @return The event details
     */
    ContractEventDetails getLatestContractEvent(String eventSignature);
}
