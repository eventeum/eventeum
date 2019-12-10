package net.consensys.eventeum.dto.message;

import java.io.Serializable;

import lombok.NoArgsConstructor;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;

@NoArgsConstructor
public class ContractEventFilterAdded extends AbstractMessage<ContractEventFilter> implements Serializable{

    public static final String TYPE = "EVENT_FILTER_ADDED";

    public ContractEventFilterAdded(ContractEventFilter filter) {
        super(filter.getId(), TYPE, filter);
    }
}