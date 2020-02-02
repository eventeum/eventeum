package io.keyko.monitoring.agent.core.chain.contract;

import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;

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
