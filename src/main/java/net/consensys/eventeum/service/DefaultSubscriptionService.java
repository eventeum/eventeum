package net.consensys.eventeum.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.contract.ContractEventListener;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.integration.broadcast.FilterEventBroadcaster;
import net.consensys.eventeum.repository.ContractEventFilterRepository;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.utils.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Subscription;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@inheritDoc}
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
public class DefaultSubscriptionService implements SubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSubscriptionService.class);

    private BlockchainService blockchainService;

    private ContractEventFilterRepository eventFilterRepository;

    private FilterEventBroadcaster filterEventBroadcaster;

    private AsyncTaskService asyncTaskService;

    private List<ContractEventListener> contractEventListeners;

    private Map<String, FilterSubscription> filterSubscriptions = new ConcurrentHashMap<>();

    @Autowired
    public DefaultSubscriptionService(BlockchainService blockchainService,
                                      ContractEventFilterRepository eventFilterRepository,
                                      FilterEventBroadcaster filterEventBroadcaster,
                                      AsyncTaskService asyncTaskService,
                                      List<BlockListener> blockListeners,
                                      List<ContractEventListener> contractEventListeners) {
        this.contractEventListeners = contractEventListeners;
        this.blockchainService = blockchainService;
        this.asyncTaskService = asyncTaskService;
        this.eventFilterRepository = eventFilterRepository;
        this.filterEventBroadcaster = filterEventBroadcaster;

        subscribeToNewBlockEvents(blockListeners);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerContractEventFilter(ContractEventFilter filter) {
        registerContractEventFilter(filter, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerContractEventFilter(ContractEventFilter filter, boolean broadcast) {
        populateIdIfMissing(filter);

        if (!isFilterRegistered(filter)) {
            registerContractEventFilter(filter, filterSubscriptions);
            saveContractEventFilter(filter);

            if (broadcast) {
                broadcastContractEventFilterAdded(filter);
            }
        } else {
            logger.info("Already registered contract event filter with id: " + filter.getId());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterContractEventFilter(String filterId) throws FilterNotFoundException {
        unregisterContractEventFilter(filterId, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterContractEventFilter(String filterId, boolean broadcast) throws FilterNotFoundException {
        final FilterSubscription filterSubscription = getFilterSubscription(filterId);

        if (filterSubscription == null) {
            throw new FilterNotFoundException(String.format("Filter with id %s, doesn't exist", filterId));
        }

        filterSubscription.getSubscription().unsubscribe();
        deleteContractEventFilter(filterSubscription.getFilter());
        removeFilterSubscription(filterId);

        if (broadcast) {
            broadcastContractEventFilterRemoved(filterSubscription.getFilter());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resubscribeToAllSubscriptions(boolean unsubscribeFirst) {
        if (unsubscribeFirst) {
            unregisterFilterSubscriptions();
        }

        final Map<String, FilterSubscription> newFilterSubscriptions = new ConcurrentHashMap<>();

        filterSubscriptions.values().forEach(filterSubscription -> {
            registerContractEventFilter(filterSubscription.getFilter(), newFilterSubscriptions);
        });

        filterSubscriptions = newFilterSubscriptions;
    }

    @PreDestroy
    private void unregisterFilterSubscriptions() {
        filterSubscriptions.values().forEach(filterSub -> filterSub.getSubscription().unsubscribe());
    }

    private void subscribeToNewBlockEvents(List<BlockListener> blockListeners) {
        blockListeners.forEach(listener -> blockchainService.addBlockListener(listener));
    }

    private void registerContractEventFilter(ContractEventFilter filter, Map<String, FilterSubscription> allFilterSubscriptions) {
        logger.info("Registering filter: " + JSON.stringify(filter));
        final Subscription sub = blockchainService.registerEventListener(filter, contractEvent -> {
            contractEventListeners.forEach(
                    listener -> asyncTaskService.execute(
                            () -> listener.onEvent(contractEvent)));
        });

        allFilterSubscriptions.put(filter.getId(), new FilterSubscription(filter, sub));
    }

    private ContractEventFilter saveContractEventFilter(ContractEventFilter contractEventFilter) {
        return eventFilterRepository.save(contractEventFilter);
    }

    private void deleteContractEventFilter(ContractEventFilter contractEventFilter) {
        eventFilterRepository.delete(contractEventFilter.getId());
    }

    private void broadcastContractEventFilterAdded(ContractEventFilter filter) {
        filterEventBroadcaster.broadcastEventFilterAdded(filter);
    }

    private void broadcastContractEventFilterRemoved(ContractEventFilter filter) {
        filterEventBroadcaster.broadcastEventFilterRemoved(filter);
    }

    private boolean isFilterRegistered(ContractEventFilter contractEventFilter) {
        return (getFilterSubscription(contractEventFilter.getId()) != null);
    }

    private FilterSubscription getFilterSubscription(String filterId) {
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

    @Data
    @AllArgsConstructor
    private class FilterSubscription {

        private ContractEventFilter filter;

        private Subscription subscription;
    }
}
