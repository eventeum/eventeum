package net.consensys.eventeum.dto.message;

import lombok.NoArgsConstructor;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;

@NoArgsConstructor
public class ContractEventFilterAdded extends AbstractMessage<ContractEventFilter> {

    public static final String TYPE = "EVENT_FILTER_ADDED";

    public ContractEventFilterAdded(ContractEventFilter filter) {
        super(filter.getId(), TYPE, filter);
    }
}