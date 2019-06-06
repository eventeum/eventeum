package net.consensys.eventeum.factory;

import net.consensys.eventeum.integration.eventstore.SaveableEventStore;

public interface EventStoreFactory {

    SaveableEventStore build();
}
