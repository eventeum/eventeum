package net.consensys.eventeum.config;

import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.block.EventStoreLatestBlockUpdater;
import net.consensys.eventeum.chain.contract.ContractEventListener;
import net.consensys.eventeum.chain.contract.EventStoreContractEventUpdater;
import net.consensys.eventeum.factory.EventStoreFactory;
import net.consensys.eventeum.integration.eventstore.EventStore;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
import net.consensys.eventeum.integration.eventstore.db.DBEventStore;
import net.consensys.eventeum.integration.eventstore.db.repository.ContractEventDetailsRepository;
import net.consensys.eventeum.integration.eventstore.db.repository.LatestBlockRepository;
import net.consensys.eventeum.integration.eventstore.rest.RESTEventStore;
import net.consensys.eventeum.integration.eventstore.rest.client.EventStoreClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@Order(0)
public class EventStoreConfiguration {

    @Configuration
    @ConditionalOnProperty(name = "eventStore.type", havingValue = "DB")
    @ConditionalOnMissingBean(EventStoreFactory.class)
    public static class DbEventStoreConfiguration {

        @Bean
        public SaveableEventStore dbEventStore(
                ContractEventDetailsRepository contractEventRepository,
                LatestBlockRepository latestBlockRepository,
                MongoTemplate mongoTemplate) {
            return new DBEventStore(contractEventRepository, latestBlockRepository, mongoTemplate);
        }

        @Bean
        public ContractEventListener eventStoreContractEventUpdater(SaveableEventStore eventStore) {
            return new EventStoreContractEventUpdater(eventStore);
        }

        @Bean
        public BlockListener eventStoreLatestBlockUpdater(SaveableEventStore eventStore) {
            return new EventStoreLatestBlockUpdater(eventStore);
        }
    }

    @Configuration
    @ConditionalOnProperty(name = "eventStore.type", havingValue = "REST")
    @ConditionalOnMissingBean(EventStoreFactory.class)
    public static class RESTEventStoreConfiguration {

        @Bean
        public EventStore RESTEventStore(EventStoreClient client) {
            return new RESTEventStore(client);
        }
    }




}
