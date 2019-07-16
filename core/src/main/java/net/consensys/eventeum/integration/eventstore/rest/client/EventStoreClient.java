package net.consensys.eventeum.integration.eventstore.rest.client;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.model.LatestBlock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@ConditionalOnProperty(name = "eventStore.type", havingValue = "REST")
@FeignClient(name="eventStore", url="${eventStore.url}")
public interface EventStoreClient {

    @RequestMapping(method = RequestMethod.GET, value="${eventStore.eventPath}")
    Page<ContractEventDetails> getContractEvents(
            @RequestParam(value = "page") int pageNo,
            @RequestParam(value = "size") int pageSize,
            @RequestParam(value = "sort") String sortAttribute,
            @RequestParam(value = "dir") Sort.Direction sortDirection,
            @RequestParam(value = "signature") String signature,
            @RequestParam(value = "contractAddress") String contractAddress);

    @RequestMapping(method = RequestMethod.GET, value="${eventStore.latestBlockPath}")
    LatestBlock getLatestBlock(@RequestParam(value = "nodeName") String nodeName);
}
