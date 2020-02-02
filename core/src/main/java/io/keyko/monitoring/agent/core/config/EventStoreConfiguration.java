package io.keyko.monitoring.agent.core.config;

import io.keyko.monitoring.agent.core.chain.block.BlockListener;
import io.keyko.monitoring.agent.core.chain.block.EventStoreLatestBlockUpdater;
import io.keyko.monitoring.agent.core.chain.contract.ContractEventListener;
import io.keyko.monitoring.agent.core.chain.contract.EventStoreContractEventUpdater;
import io.keyko.monitoring.agent.core.chain.factory.BlockDetailsFactory;
import io.keyko.monitoring.agent.core.chain.service.container.ChainServicesContainer;
import io.keyko.monitoring.agent.core.factory.EventStoreFactory;
import io.keyko.monitoring.agent.core.integration.eventstore.EventStore;
import io.keyko.monitoring.agent.core.integration.eventstore.SaveableEventStore;
import io.keyko.monitoring.agent.core.integration.eventstore.db.MongoEventStore;
import io.keyko.monitoring.agent.core.integration.eventstore.db.SqlEventStore;
import io.keyko.monitoring.agent.core.integration.eventstore.db.repository.ContractEventDetailsRepository;
import io.keyko.monitoring.agent.core.integration.eventstore.db.repository.LatestBlockRepository;
import io.keyko.monitoring.agent.core.integration.eventstore.rest.RESTEventStore;
import io.keyko.monitoring.agent.core.integration.eventstore.rest.client.EventStoreClient;
import io.keyko.monitoring.agent.core.monitoring.EventeumValueMonitor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Order(0)
public class EventStoreConfiguration {

    @Configuration
    @ConditionalOnExpression("'${eventStore.type}:${database.type}'=='DB:MONGO'")
    @ConditionalOnMissingBean(EventStoreFactory.class)
    public static class MongoEventStoreConfiguration {

        @Bean
        public SaveableEventStore dbEventStore(
                ContractEventDetailsRepository contractEventRepository,
                LatestBlockRepository latestBlockRepository,
                MongoTemplate mongoTemplate) {
            return new MongoEventStore(contractEventRepository, latestBlockRepository, mongoTemplate);
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

    @Configuration
    @ConditionalOnExpression("'${eventStore.type}:${database.type}'=='DB:SQL'")
    @ConditionalOnMissingBean(EventStoreFactory.class)
    public static class SqlEventStoreConfiguration {

        @Bean
        public SaveableEventStore dbEventStore(
                ContractEventDetailsRepository contractEventRepository,
                LatestBlockRepository latestBlockRepository,
                JdbcTemplate jdbcTemplate) {
            return new SqlEventStore(contractEventRepository, latestBlockRepository, jdbcTemplate);
        }

        @Bean
        public ContractEventListener eventStoreContractEventUpdater(SaveableEventStore eventStore) {
            return new EventStoreContractEventUpdater(eventStore);
        }

        @Bean
        public BlockListener eventStoreLatestBlockUpdater(SaveableEventStore eventStore,
                                                          BlockDetailsFactory blockDetailsFactory,
                                                          EventeumValueMonitor valueMonitor,
                                                          ChainServicesContainer chainServiceContainer) {
            return new EventStoreLatestBlockUpdater(eventStore, blockDetailsFactory, valueMonitor, chainServiceContainer);
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
