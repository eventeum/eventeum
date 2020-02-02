package io.keyko.monitoring.agent.core.config;

import io.keyko.monitoring.agent.core.chain.block.BlockListener;
import io.keyko.monitoring.agent.core.chain.block.EventStoreLatestBlockUpdater;
import io.keyko.monitoring.agent.core.chain.contract.ContractEventListener;
import io.keyko.monitoring.agent.core.chain.contract.EventStoreContractEventUpdater;
import io.keyko.monitoring.agent.core.chain.factory.BlockDetailsFactory;
import io.keyko.monitoring.agent.core.chain.service.container.ChainServicesContainer;
import io.keyko.monitoring.agent.core.factory.EventStoreFactory;
import io.keyko.monitoring.agent.core.integration.eventstore.SaveableEventStore;
import io.keyko.monitoring.agent.core.monitoring.EventeumValueMonitor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Order(1)
@ConditionalOnBean(EventStoreFactory.class)
public class CustomEventStoreConfiguration {

    @Bean
    public SaveableEventStore customEventStore(EventStoreFactory factory) {
        return factory.build();
    }

    @Bean
    public ContractEventListener eventStoreContractEventUpdater(SaveableEventStore eventStore) {
        return new EventStoreContractEventUpdater(eventStore);
    }

    @Bean
    public BlockListener eventStoreLatestBlockUpdater(SaveableEventStore eventStore,
                                                      BlockDetailsFactory blockDetailsFactory,
                                                      EventeumValueMonitor valueMonitor,
                                                      ChainServicesContainer chainServicesContainer) {
        return new EventStoreLatestBlockUpdater(eventStore, blockDetailsFactory, valueMonitor, chainServicesContainer);
    }
}
