package io.keyko.monitoring.agent.core.integration.broadcast.internal;

import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import io.keyko.monitoring.agent.core.model.TransactionMonitoringSpec;

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
