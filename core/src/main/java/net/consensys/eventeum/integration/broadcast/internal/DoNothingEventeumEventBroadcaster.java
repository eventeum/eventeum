package net.consensys.eventeum.integration.broadcast.internal;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.model.TransactionMonitoringSpec;

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
