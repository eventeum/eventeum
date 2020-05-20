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
import net.consensys.eventeum.chain.contract.ContractEventListener;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.container.NodeServices;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.integration.broadcast.internal.EventeumEventBroadcaster;
import net.consensys.eventeum.model.FilterSubscription;
import net.consensys.eventeum.repository.ContractEventFilterRepository;
import net.consensys.eventeum.service.exception.NotFoundException;
import net.consensys.eventeum.utils.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    private AsyncTaskService asyncTaskService;

    private List<ContractEventListener> contractEventListeners;

    private List<BlockListener> blockListeners;

    private Map<String, ContractEventFilter> filterSubscriptions;

    private ApplicationContext applicationContext;

    private RetryTemplate retryTemplate;

    @Autowired
    public DefaultSubscriptionService(ChainServicesContainer chainServices,
                                      ContractEventFilterRepository eventFilterRepository,
                                      EventeumEventBroadcaster eventeumEventBroadcaster,
                                      AsyncTaskService asyncTaskService,
                                      List<BlockListener> blockListeners,
                                      List<ContractEventListener> contractEventListeners,
                                      @Qualifier("eternalRetryTemplate") RetryTemplate retryTemplate) {
        this.contractEventListeners = contractEventListeners;
        this.chainServices = chainServices;
        this.asyncTaskService = asyncTaskService;
        this.eventFilterRepository = eventFilterRepository;
        this.eventeumEventBroadcaster = eventeumEventBroadcaster;
        this.blockListeners = blockListeners;
        this.retryTemplate = retryTemplate;

        filterSubscriptions = new HashMap<>();
    }


    public void init() {
        chainServices.getNodeNames().forEach(nodeName -> subscribeToNewBlockEvents(
                chainServices.getNodeServices(nodeName).getBlockchainService(), blockListeners));
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
            BlockchainService blockchainService, List<BlockListener> blockListeners) {
        blockListeners.forEach(listener -> blockchainService.addBlockListener(listener));

        blockchainService.connect();
    }

    private void triggerListener(ContractEventListener listener, ContractEventDetails contractEventDetails) {
        try {
            listener.onEvent(contractEventDetails);
        } catch (Throwable t) {
            log.error(String.format(
                    "An error occurred when processing contractEvent with id %s", contractEventDetails.getId()), t);
        }
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
