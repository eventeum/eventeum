package net.consensys.eventeum.chain.contract;

import net.consensys.eventeum.ContractEventDetails;

/**
 * A listener for new contract events.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface ContractEventListener {

    /**
     * Called when an event is fired for any configured contract events within the system.
     *
     * @param eventDetails The details of the new event.
     */
    void onEvent(ContractEventDetails eventDetails);
}
