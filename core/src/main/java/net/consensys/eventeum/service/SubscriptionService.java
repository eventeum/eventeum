package net.consensys.eventeum.service;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.service.exception.NotFoundException;

import java.util.List;

/**
 * A service for manageing contract event subscriptions within the Eventeum instance.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface SubscriptionService {

    /**
     * Initialise the subscription service
     */
    void init();

    /**
     * Registers a new contract event filter.
     *
     * If the id is null, then one is assigned.
     *
     * @param filter The filter to add.
     * @param broadcast Specifies if the added filter event should be broadcast to other Eventeum instances.
     * @return The registered contract event filter
     */
    ContractEventFilter registerContractEventFilter(ContractEventFilter filter, boolean broadcast);

    /**
     * Registers a new contract event filter.
     *
     * If the id is null, then one is assigned.
     *
     * Will retry indefinitely until successful
     *
     * @param filter The filter to add.
     * @param broadcast Specifies if the added filter event should be broadcast to other Eventeum instances.
     * @return The registered contract event filter
     */
    ContractEventFilter registerContractEventFilterWithRetries(ContractEventFilter filter, boolean broadcast);
    /**
     * List all registered contract event filters.
     *
     * @return The list of registered contract event filters
     */
    List<ContractEventFilter> listContractEventFilters();

    /**
     * Unregisters a previously added contract event filter.
     *
     * Broadcasts the removed filter event to any other Eventeum instances.
     *
     * @param filterId The filter id of the event to remove.
     */
    void unregisterContractEventFilter(String filterId) throws NotFoundException;

    /**
     * Unregisters a previously added contract event filter.
     *
     * @param filterId The filter id of the event to remove.
     * @param broadcast Specifies if the removed filter event should be broadcast to other Eventeum instances.
     */
    void unregisterContractEventFilter(String filterId, boolean broadcast) throws NotFoundException;

    /**
     * Resubscribe to all currently active event filters.
     */
    void resubscribeToAllSubscriptions(String nodeName);

    /**
     * Unsubscribe all active listeners
     */
    void unsubscribeToAllSubscriptions(String nodeName);

    /**
     * Returns true if all subscriptions for node are active (not disposed)
     *
     * @param nodeName The node name
     * @return true if all subscriptions for node are active (not disposed)
     */
    boolean isFullySubscribed(String nodeName);
}
