package io.keyko.monitoring.agent.core.integration.eventstore;

import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.model.LatestBlock;

/**
 * Interface for integrating with an event store that supports direct saving of events.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface SaveableEventStore extends EventStore {
    void save(ContractEventDetails contractEventDetails);

    void save(LatestBlock latestBlock);
}
