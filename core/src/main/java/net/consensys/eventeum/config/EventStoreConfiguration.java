package net.consensys.eventeum.config;

import net.consensys.eventeum.chain.contract.ContractEventListener;
import net.consensys.eventeum.chain.contract.SaveableEventStoreUpdater;
import net.consensys.eventeum.factory.EventStoreFactory;
import net.consensys.eventeum.integration.eventstore.EventStore;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
import net.consensys.eventeum.integration.eventstore.db.DBEventStore;
import net.consensys.eventeum.integration.eventstore.db.repository.ContractEventDetailsRepository;
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
                ContractEventDetailsRepository repository, MongoTemplate mongoTemplate) {
            return new DBEventStore(repository, mongoTemplate);
        }

        @Bean
        public ContractEventListener saveableEventStoreUpdater(SaveableEventStore eventStore) {
            return new SaveableEventStoreUpdater(eventStore);
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
