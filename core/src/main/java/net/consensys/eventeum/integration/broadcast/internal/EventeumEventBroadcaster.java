package net.consensys.eventeum.integration.broadcast.internal;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.model.TransactionMonitoringSpec;

/**
 * An interface for a class that broadcasts Eventeum internal events to other Eventeum instances in the wider system.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface EventeumEventBroadcaster {

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

    /**
     * Broadcasts the details of a transaction monitoring spec that has been added to this Eventeum instance.
     *
     * @param spec the transaction monitoring spec in question.
     */
    void broadcastTransactionMonitorAdded(TransactionMonitoringSpec spec);

    /**
     * Broadcasts the details of a transaction monitoring spec that has been removed from this Eventeum instance.
     *
     * @param spec the transaction monitoring spec in question.
     */
    void broadcastTransactionMonitorRemoved(TransactionMonitoringSpec spec);
}
