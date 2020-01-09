package net.consensys.eventeum.service;

import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.contract.ContractEventListener;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.container.NodeServices;
import net.consensys.eventeum.ContractEventDetails;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    private Map<String, FilterSubscription> filterSubscriptions = new ConcurrentHashMap<>();

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
        return getFilterSubscriptions().stream().map((FilterSubscription f) -> f.getFilter()).collect(Collectors.toList());
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
        final FilterSubscription filterSubscription = getFilterSubscription(filterId);

        if (filterSubscription == null) {
            throw new NotFoundException(String.format("Filter with id %s, doesn't exist", filterId));
        }

        unsubscribeFilterSubscription(filterSubscription);

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
    public void resubscribeToAllSubscriptions() {
        final List<ContractEventFilter> currentFilters = filterSubscriptions
                .values()
                .stream()
                .map(filterSubscription -> filterSubscription.getFilter())
                .collect(Collectors.toList());

        final Map<String, FilterSubscription> newFilterSubscriptions = new ConcurrentHashMap<>();

        currentFilters.forEach(filter -> registerContractEventFilter(filter, newFilterSubscriptions));

        filterSubscriptions = newFilterSubscriptions;

        log.info("Resubscribed to event filters: {}", JSON.stringify(filterSubscriptions));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribeToAllSubscriptions(String nodeName) {
        filterSubscriptions.values().forEach(filterSub -> {
            if (filterSub.getFilter().getNode().equals(nodeName)) {
                unsubscribeFilterSubscription(filterSub);
            }
        });
    }

    @Override
    public boolean isFullySubscribed(String nodeName) {
        return !filterSubscriptions
                .values()
                .stream()
                .filter(filterSubscription -> filterSubscription.getFilter().getNode().equals(nodeName))
                .filter(filterSubscription -> filterSubscription.getSubscription().isDisposed())
                .findFirst()
                .isPresent();
    }

    @PreDestroy
    private void unregisterAllContractEventFilters() {
        filterSubscriptions.values().forEach(filterSub -> {
            unsubscribeFilterSubscription(filterSub);
        });
    }

    public void unsubscribeFilterSubscription(FilterSubscription filterSubscription) {

        try {
            filterSubscription.getSubscription().dispose();
        } catch (Throwable t) {
            log.info("Unable to unregister filter...this is probably because the " +
                    "node has restarted or we're in websocket mode");
        }
    }

    private ContractEventFilter doRegisterContractEventFilter(ContractEventFilter filter, boolean broadcast) {
        populateIdIfMissing(filter);

        if (!isFilterRegistered(filter)) {
            final FilterSubscription sub = registerContractEventFilter(filter, filterSubscriptions);

            if (filter.getStartBlock() == null && sub != null) {
                filter.setStartBlock(sub.getStartBlock());
            }

            saveContractEventFilter(filter);

            if (broadcast) {
                broadcastContractEventFilterAdded(filter);
            }

            return filter;
        } else {
            log.info("Already registered contract event filter with id: " + filter.getId());
            return getFilterSubscription(filter.getId()).getFilter();
        }
    }

    private void subscribeToNewBlockEvents(
            BlockchainService blockchainService, List<BlockListener> blockListeners) {
        blockListeners.forEach(listener -> blockchainService.addBlockListener(listener));

        blockchainService.connect();
    }

    private FilterSubscription registerContractEventFilter(ContractEventFilter filter, Map<String, FilterSubscription> allFilterSubscriptions) {
        log.info("Registering filter: " + JSON.stringify(filter));

        final NodeServices nodeServices = chainServices.getNodeServices(filter.getNode());

        if (nodeServices == null) {
            log.warn("No node configure" +
                    "d with name {}, not registering filter", filter.getNode());
            return null;
        }

        final BlockchainService blockchainService = nodeServices.getBlockchainService();

        final FilterSubscription sub = blockchainService.registerEventListener(filter, contractEvent -> {
            contractEventListeners.forEach(
                    listener -> triggerListener(listener, contractEvent));
        });

        allFilterSubscriptions.put(filter.getId(), sub);

        log.debug("Registered filters: {}", JSON.stringify(allFilterSubscriptions));

        return sub;
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
        return (getFilterSubscription(contractEventFilter.getId()) != null);
    }

    private FilterSubscription getFilterSubscription(String filterId) {
        return filterSubscriptions.get(filterId);
    }

    private List<FilterSubscription> getFilterSubscriptions() {
        return new ArrayList(filterSubscriptions.values());
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
