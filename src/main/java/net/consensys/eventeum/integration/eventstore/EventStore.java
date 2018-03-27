package net.consensys.eventeum.integration.eventstore;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Interface for integrating with an event store, in order to obtain events for a specified signature.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface EventStore {
    Page<ContractEventDetails> getContractEventsForSignature(String eventSignature, PageRequest pagination);
}
