package net.consensys.eventeum.config;

import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.block.EventStoreLatestBlockUpdater;
import net.consensys.eventeum.chain.contract.ContractEventListener;
import net.consensys.eventeum.chain.contract.EventStoreContractEventUpdater;
import net.consensys.eventeum.chain.factory.BlockDetailsFactory;
import net.consensys.eventeum.factory.EventStoreFactory;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
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
    public BlockListener eventStoreLatestBlockUpdater(
            SaveableEventStore eventStore, BlockDetailsFactory blockDetailsFactory) {
        return new EventStoreLatestBlockUpdater(eventStore, blockDetailsFactory);
    }
}
