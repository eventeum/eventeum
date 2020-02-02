package io.keyko.monitoring.agent.core.chain.contract;

import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.integration.eventstore.SaveableEventStore;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A contract event listener that saves the ContractEventDetails to a SaveableEventStore.
 * <p>
 * Only gets registered if a SaveableEventStore exists in the context.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class EventStoreContractEventUpdater implements ContractEventListener {

    private SaveableEventStore saveableEventStore;

    @Autowired
    public EventStoreContractEventUpdater(SaveableEventStore saveableEventStore) {
        this.saveableEventStore = saveableEventStore;
    }

    @Override
    public void onEvent(ContractEventDetails eventDetails) {
        saveableEventStore.save(eventDetails);
    }
}
