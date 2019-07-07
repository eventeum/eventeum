package net.consensys.eventeum.integration.eventstore;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.model.LatestBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Interface for integrating with an event store that supports direct saving of events.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface SaveableEventStore extends EventStore {
    void save(ContractEventDetails contractEventDetails);

    void save(LatestBlock latestBlock);
}
