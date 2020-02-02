package io.keyko.monitoring.agent.core.factory;

import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import org.springframework.data.repository.CrudRepository;

public interface ContractEventFilterRepositoryFactory {

    CrudRepository<ContractEventFilter, String> build();
}
