package io.keyko.monitoring.agent.core.integration.eventstore.rest;

import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.integration.eventstore.EventStore;
import io.keyko.monitoring.agent.core.integration.eventstore.rest.client.EventStoreClient;
import io.keyko.monitoring.agent.core.model.LatestBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;

/**
 * An event store implementation that integrates with an external REST api in order to obtain the event details.
 * <p>
 * The REST events tore path can be specified with the eventStore.url and eventStore.eventPath parameters.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class RESTEventStore implements EventStore {

    private EventStoreClient client;

    public RESTEventStore(EventStoreClient client) {
        this.client = client;
    }

    @Override
    public Page<ContractEventDetails> getContractEventsForSignature(
            String eventSignature, String contractAddress, PageRequest pagination) {

        final Sort.Order firstOrder = pagination.getSort().iterator().next();
        return client.getContractEvents(pagination.getPageNumber(),
                pagination.getPageSize(),
                firstOrder.getProperty(),
                firstOrder.getDirection(),
                eventSignature,
                contractAddress);
    }

    @Override
    public Optional<LatestBlock> getLatestBlockForNode(String nodeName) {
        return Optional.ofNullable(client.getLatestBlock(nodeName));
    }

    @Override
    public boolean isPagingZeroIndexed() {
        return true;
    }
}
