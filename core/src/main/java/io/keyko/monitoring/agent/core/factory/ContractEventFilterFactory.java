package io.keyko.monitoring.agent.core.factory;

import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;

import java.util.List;

public interface ContractEventFilterFactory {

    List<ContractEventFilter> build();
}
