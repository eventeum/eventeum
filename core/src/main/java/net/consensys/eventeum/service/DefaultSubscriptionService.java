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

import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.strategy.BlockSubscriptionStrategy;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.integration.broadcast.internal.EventeumEventBroadcaster;
import net.consensys.eventeum.repository.ContractEventFilterRepository;
import net.consensys.eventeum.service.exception.NotFoundException;
import net.consensys.eventeum.service.sync.EventSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * {@inheritDoc}
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Slf4j
@Component
public class DefaultSubscriptionService implements SubscriptionService {

    private ChainServicesContainer chainServices;

    private ContractEventFilterRepository eventFilterRepository;

    private EventeumEventBroadcaster eventeumEventBroadcaster;

    private List<BlockListener> blockListeners;

    private Map<String, ContractEventFilter> filterSubscriptions;

    private RetryTemplate retryTemplate;

    private EventSyncService eventSyncService;

    private SubscriptionServiceState state = SubscriptionServiceState.UNINITIALISED;

    @Autowired
    public DefaultSubscriptionService(ChainServicesContainer chainServices,
                                      ContractEventFilterRepository eventFilterRepository,
                                      EventeumEventBroadcaster eventeumEventBroadcaster,
                                      List<BlockListener> blockListeners,
                                      @Qualifier("eternalRetryTemplate") RetryTemplate retryTemplate,
                                      EventSyncService eventSyncService) {
        this.chainServices = chainServices;
        this.eventFilterRepository = eventFilterRepository;
        this.eventeumEventBroadcaster = eventeumEventBroadcaster;
        this.blockListeners = blockListeners;
        this.retryTemplate = retryTemplate;
        this.eventSyncService = eventSyncService;

        filterSubscriptions = new HashMap<>();
    }


    public void init(List<ContractEventFilter> initFilters) {

        if (initFilters != null && !initFilters.isEmpty()) {
            final List<ContractEventFilter> filtersWithStartBlock = initFilters
                    .stream()
                    .filter(filter -> filter.getStartBlock() != null)
                    .collect(Collectors.toList());

            if (!filtersWithStartBlock.isEmpty()) {
                state = SubscriptionServiceState.SYNCING_EVENTS;
                eventSyncService.sync(filtersWithStartBlock);
            }
        }

        chainServices.getNodeNames().forEach(nodeName -> subscribeToNewBlockEvents(
                chainServices.getNodeServices(nodeName).getBlockSubscriptionStrategy(), blockListeners));

        state = SubscriptionServiceState.SUBSCRIBED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContractEventFilter registerContractEventFilter(ContractEventFilter filter, boolean broadcast) {
        return doRegisterContractEventFilter(filter, broadcast);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Async
    public ContractEventFilter registerContractEventFilterWithRetries(ContractEventFilter filter, boolean broadcast) {
        return retryTemplate.execute((context) -> doRegisterContractEventFilter(filter, broadcast));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ContractEventFilter> listContractEventFilters() {
      return new ArrayList<>(filterSubscriptions.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterContractEventFilter(String filterId) throws NotFoundException {
        unregisterContractEventFilter(filterId, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterContractEventFilter(String filterId, boolean broadcast) throws NotFoundException {
        final ContractEventFilter filterToUnregister = getRegisteredFilter(filterId);

        if (filterToUnregister == null) {
            throw new NotFoundException(String.format("Filter with id %s, doesn't exist", filterId));
        }

        deleteContractEventFilter(filterToUnregister);
        removeFilterSubscription(filterId);

        if (broadcast) {
            broadcastContractEventFilterRemoved(filterToUnregister);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribeToAllSubscriptions(String nodeName) {
        filterSubscriptions
                .entrySet()
                .removeIf(entry -> entry.getValue().getNode().equals(nodeName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionServiceState getState() {
        return state;
    }

    private ContractEventFilter doRegisterContractEventFilter(ContractEventFilter filter, boolean broadcast) {
        try {
            populateIdIfMissing(filter);

            if (!isFilterRegistered(filter)) {
                filterSubscriptions.put(filter.getId(), filter);

                //TODO start block replay

                saveContractEventFilter(filter);

                if (broadcast) {
                    broadcastContractEventFilterAdded(filter);
                }

                return filter;
            } else {
                log.info("Already registered contract event filter with id: " + filter.getId());
                return getRegisteredFilter(filter.getId());
            }
        } catch (Exception e) {
            log.error("Error registering filter " + filter.getId(), e);
            throw e;
        }
    }

    private void subscribeToNewBlockEvents(
            BlockSubscriptionStrategy subscriptionStrategy, List<BlockListener> blockListeners) {
        blockListeners.forEach(listener -> subscriptionStrategy.addBlockListener(listener));

        subscriptionStrategy.subscribe();
    }

    private ContractEventFilter saveContractEventFilter(ContractEventFilter contractEventFilter) {
        return eventFilterRepository.save(contractEventFilter);
    }

    private void deleteContractEventFilter(ContractEventFilter contractEventFilter) {
        eventFilterRepository.deleteById(contractEventFilter.getId());
    }

    private void broadcastContractEventFilterAdded(ContractEventFilter filter) {
        eventeumEventBroadcaster.broadcastEventFilterAdded(filter);
    }

    private void broadcastContractEventFilterRemoved(ContractEventFilter filter) {
        eventeumEventBroadcaster.broadcastEventFilterRemoved(filter);
    }

    private boolean isFilterRegistered(ContractEventFilter contractEventFilter) {
        return (getRegisteredFilter(contractEventFilter.getId()) != null);
    }

    private ContractEventFilter getRegisteredFilter(String filterId) {
        return filterSubscriptions.get(filterId);
    }

    private void removeFilterSubscription(String filterId) {
        filterSubscriptions.remove(filterId);
    }

    private void populateIdIfMissing(ContractEventFilter filter) {
        if (filter.getId() == null) {
            filter.setId(UUID.randomUUID().toString());
        }
    }
}
