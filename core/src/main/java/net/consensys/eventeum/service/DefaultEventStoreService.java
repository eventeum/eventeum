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

package net.consensys.eventeum.service;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.integration.eventstore.EventStore;
import net.consensys.eventeum.model.LatestBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

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
