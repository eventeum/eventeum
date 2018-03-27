package net.consensys.eventeum.service;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.integration.eventstore.EventStore;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @{inheritDoc}
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
public class DefaultEventStoreService implements EventStoreService {

    private EventStore eventStore;

    public DefaultEventStoreService(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public ContractEventDetails getLatestContractEvent(String eventSignature) {
        final PageRequest pagination = new PageRequest(1,
                1, new Sort(Sort.Direction.DESC, "blockNumber"));

        final List<ContractEventDetails> events =
                eventStore.getContractEventsForSignature(eventSignature, pagination).getContent();

        if (events == null || events.isEmpty()) {
            return null;
        }

        return events.get(0);
    }
}
