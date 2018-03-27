package net.consensys.eventeum.integration.broadcast;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;

/**
 * An interface for a class that broadcasts Eventeum filter events to other Eventeum instances in the wider system.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface FilterEventBroadcaster {

    /**
     * Broadcasts the details of a contract event filter that has been added to this Eventeum instance.
     *
     * @param filter the filter in question.
     */
    void broadcastEventFilterAdded(ContractEventFilter filter);

    /**
     * Broadcasts the details of a contract event filter that has been removed from this Eventeum instance.
     *
     * @param filter the filter in question.
     */
    void broadcastEventFilterRemoved(ContractEventFilter filter);
}
