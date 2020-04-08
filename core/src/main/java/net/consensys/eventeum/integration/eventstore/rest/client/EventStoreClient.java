/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
