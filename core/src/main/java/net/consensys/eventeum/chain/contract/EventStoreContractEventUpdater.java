package net.consensys.eventeum.chain.contract;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A contract event listener that saves the ContractEventDetails to a SaveableEventStore.
 *
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
