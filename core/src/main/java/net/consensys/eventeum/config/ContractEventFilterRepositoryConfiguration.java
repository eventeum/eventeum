package net.consensys.eventeum.config;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.factory.ContractEventFilterRepositoryFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.CrudRepository;

@Configuration
public class ContractEventFilterRepositoryConfiguration {

    @Bean
    @ConditionalOnBean(ContractEventFilterRepositoryFactory.class)
    public CrudRepository<ContractEventFilter, String> customContractEventFilterRepository(
            ContractEventFilterRepositoryFactory factory) {
        return factory.build();
    }
}
