package io.keyko.monitoring.agent.core.integration.broadcast.internal;

import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import io.keyko.monitoring.agent.core.model.TransactionMonitoringSpec;

/**
 * A dummy broadcaster that does nothing.
 * <p>
 * (Used in single instance mode)
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class DoNothingEventeumEventBroadcaster implements EventeumEventBroadcaster {

    @Override
    public void broadcastEventFilterAdded(ContractEventFilter filter) {
        //DO NOTHING!
    }

    @Override
    public void broadcastEventFilterRemoved(ContractEventFilter filter) {
        //DO NOTHING!
    }

    @Override
    public void broadcastTransactionMonitorAdded(TransactionMonitoringSpec spec) {
        //DO NOTHING!
    }

    @Override
    public void broadcastTransactionMonitorRemoved(TransactionMonitoringSpec spec) {
        //DO NOTHING
    }
}
