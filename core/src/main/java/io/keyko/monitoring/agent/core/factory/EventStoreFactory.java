package io.keyko.monitoring.agent.core.factory;

import io.keyko.monitoring.agent.core.integration.eventstore.SaveableEventStore;

public interface EventStoreFactory {

    SaveableEventStore build();
}
