package net.consensys.eventeum.factory;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;

import java.util.List;

public interface ContractEventFilterFactory {

    List<ContractEventFilter> build();
}
