package io.keyko.monitoring.agent.core.service;

import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.integration.eventstore.EventStore;
import io.keyko.monitoring.agent.core.model.LatestBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @author Craig Williams <craig.williams@consensys.net>
 * @{inheritDoc}
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
    public Optional<ContractEventDetails> getLatestContractEvent(
            String eventSignature, String contractAddress) {
        int page = eventStore.isPagingZeroIndexed() ? 0 : 1;

        final PageRequest pagination = PageRequest.of(page,
                1, Sort.by(Sort.Direction.DESC, "blockNumber"));

        final Page<ContractEventDetails> eventsPage =
                eventStore.getContractEventsForSignature(eventSignature, contractAddress, pagination);

        if (eventsPage == null) {
            return Optional.empty();
        }

        final List<ContractEventDetails> events = eventsPage.getContent();

        if (events == null || events.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(events.get(0));
    }

    @Override
    public Optional<LatestBlock> getLatestBlock(String nodeName) {

        return eventStore.getLatestBlockForNode(nodeName);
    }
}
