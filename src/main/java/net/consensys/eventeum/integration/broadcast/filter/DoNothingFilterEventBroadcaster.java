package net.consensys.eventeum.integration.broadcast.filter;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.integration.broadcast.filter.FilterEventBroadcaster;

/**
 * A dummy broadcaster that does nothing.
 *
 * (Used in single instance mode)
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class DoNothingFilterEventBroadcaster implements FilterEventBroadcaster {

    @Override
    public void broadcastEventFilterAdded(ContractEventFilter filter) {
        //DO NOTHING!
    }

    @Override
    public void broadcastEventFilterRemoved(ContractEventFilter filter) {
        //DO NOTHING!
    }
}
