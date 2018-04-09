package net.consensys.eventeum.chain.contract;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * A contract event listener that saves the ContractEventDetails to a SaveableEventStore.
 *
 * Only gets registered if a SaveableEventStore exists in the context.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
@ConditionalOnBean(SaveableEventStore.class)
public class SaveableEventStoreUpdater implements ContractEventListener {

    private SaveableEventStore saveableEventStore;

    @Autowired
    public SaveableEventStoreUpdater(SaveableEventStore saveableEventStore) {
        this.saveableEventStore = saveableEventStore;
    }
    @Override
    public void onEvent(ContractEventDetails eventDetails) {
        saveableEventStore.save(eventDetails);
    }
}
