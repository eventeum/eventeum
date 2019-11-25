package net.consensys.eventeum.chain.contract;

import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;

import java.util.List;

public interface ContractEventProcessor {

    void processLogsInBlock(Block block, List<ContractEventFilter> contractEventFilters);
}
