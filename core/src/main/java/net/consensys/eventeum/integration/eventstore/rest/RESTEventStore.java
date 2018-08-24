package net.consensys.eventeum.integration.eventstore.rest;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.integration.eventstore.EventStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * An event store implementation that integrates with an external REST api in order to obtain the event details.
 *
 * The REST events tore path can be specified with the eventStore.url and eventStore.eventPath parameters.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
@ConditionalOnProperty(name = "eventStore.type", havingValue = "REST")
public class RESTEventStore implements EventStore {

    private FeignEventStore integration;

    public RESTEventStore(FeignEventStore integration) {
        this.integration = integration;
    }

    @Override
    public Page<ContractEventDetails> getContractEventsForSignature(String eventSignature, PageRequest pagination) {

        final Sort.Order firstOrder = pagination.getSort().iterator().next();
        return integration.getContractEvents(pagination.getPageNumber(),
                                             pagination.getPageSize(),
                                             firstOrder.getProperty(),
                                             firstOrder.getDirection(),
                                             eventSignature);
    }

    @Override
    public boolean isPagingZeroIndexed() {
        return true;
    }

    @ConditionalOnProperty(name = "eventStore.type", havingValue = "REST")
    @FeignClient(name="eventStore", url="${eventStore.url}")
    private interface FeignEventStore {

        @RequestMapping(method = RequestMethod.GET, value="${eventStore.eventPath}")
        Page<ContractEventDetails> getContractEvents(
                @RequestParam(value = "page") int pageNo,
                @RequestParam(value = "size") int pageSize,
                @RequestParam(value = "sort") String sortAttribute,
                @RequestParam(value = "dir") Sort.Direction sortDirection,
                @RequestParam(value = "signature") String signature);
    }
}
