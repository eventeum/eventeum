package net.consensys.eventeum.integration.eventstore.rest;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.factory.EventStoreFactory;
import net.consensys.eventeum.integration.eventstore.EventStore;
import net.consensys.eventeum.integration.eventstore.rest.client.EventStoreClient;
import net.consensys.eventeum.model.LatestBlock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * An event store implementation that integrates with an external REST api in order to obtain the event details.
 *
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
    public Page<ContractEventDetails> getContractEventsForSignature(String eventSignature, PageRequest pagination) {

        final Sort.Order firstOrder = pagination.getSort().iterator().next();
        return client.getContractEvents(pagination.getPageNumber(),
                                             pagination.getPageSize(),
                                             firstOrder.getProperty(),
                                             firstOrder.getDirection(),
                                             eventSignature);
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
