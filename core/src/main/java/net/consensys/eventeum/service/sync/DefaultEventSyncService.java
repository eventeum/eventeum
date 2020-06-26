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

package net.consensys.eventeum.service.sync;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.contract.ContractEventListener;
import net.consensys.eventeum.chain.service.block.BlockNumberService;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.model.EventFilterSyncStatus;
import net.consensys.eventeum.model.SyncStatus;
import net.consensys.eventeum.repository.EventFilterSyncStatusRepository;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class DefaultEventSyncService implements EventSyncService {

    private List<ContractEventListener> contractEventListeners;

    private BlockNumberService blockNumberService;

    private EventRetriever eventRetriever;

    private EventFilterSyncStatusRepository syncStatusRepository;

    @Override
    public void catchup(List<ContractEventFilter> filters) {

        filters.forEach(filter -> {

            final Optional<EventFilterSyncStatus> syncStatus = syncStatusRepository.findById(filter.getId());

            if (!syncStatus.isPresent() || syncStatus.get().getSyncStatus() == SyncStatus.NOT_SYNCED) {
                log.info("Syncing event filter with id {}", filter.getId());

                //Should catchup to start block
                final BigInteger endBlock = blockNumberService.getStartBlockForNode(filter.getNode());

                eventRetriever.retrieveEvents(filter, getStartBlock(filter, syncStatus), endBlock,
                        (events) -> events.forEach(this::processEvent));

                final EventFilterSyncStatus finalSyncStatus = getEventSyncStatus(filter.getId());
                finalSyncStatus.setSyncStatus(SyncStatus.SYNCED);
                syncStatusRepository.save(finalSyncStatus);

                log.info("Event filter with id {} has completed syncing", filter.getId());

            } else {
                log.info("Event filter with id {} already synced", filter.getId());
            }
        });

    }

    private void processEvent(ContractEventDetails contractEvent) {
        triggerListeners(contractEvent);

        final EventFilterSyncStatus syncStatus = getEventSyncStatus(contractEvent.getFilterId());

        syncStatus.setLastBlockNumber(contractEvent.getBlockNumber());
        syncStatusRepository.save(syncStatus);
    }

    private EventFilterSyncStatus getEventSyncStatus(String id) {
        return syncStatusRepository.findById(id)
                .orElse(EventFilterSyncStatus
                        .builder()
                        .filterId(id)
                        .syncStatus(SyncStatus.NOT_SYNCED)
                        .build());
    }

    private void triggerListeners(ContractEventDetails contractEvent) {
        contractEventListeners.forEach(
                listener -> triggerListener(listener, contractEvent));
    }

    private void triggerListener(ContractEventListener listener, ContractEventDetails contractEventDetails) {
        try {
            listener.onEvent(contractEventDetails);
        } catch (Throwable t) {
            log.error(String.format(
                    "An error occurred when processing contractEvent with id %s", contractEventDetails.getId()), t);
        }
    }

    private BigInteger getStartBlock(ContractEventFilter contractEventFilter,
                                     Optional<EventFilterSyncStatus> syncStatus) {

        if (syncStatus.isPresent() && syncStatus.get().getLastBlockNumber().compareTo(BigInteger.ZERO) > 0) {
            return syncStatus.get().getLastBlockNumber();
        }

        return contractEventFilter.getStartBlock();
    }
}
