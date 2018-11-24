package net.consensys.eventeum.factory;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import org.springframework.data.repository.CrudRepository;

public interface ContractEventFilterRepositoryFactory {

    CrudRepository<ContractEventFilter, String> build();
}
