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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventStoreConfiguration {

    @Bean
    @ConditionalOnProperty(name = "eventStore.type", havingValue = "DB")
    @ConditionalOnMissingBean(EventStoreFactory.class)
    public EventStore dbEventStore(ContractEventDetailsRepository repository) {
        return new DBEventStore(repository);
    }

    @Bean
    @ConditionalOnProperty(name = "eventStore.type", havingValue = "REST")
    @ConditionalOnMissingBean(EventStoreFactory.class)
    public EventStore RESTEventStore(EventStoreClient client) {
        return new RESTEventStore(client);
    }

    @Bean
    @ConditionalOnBean(EventStoreFactory.class)
    public SaveableEventStore customEventStore(EventStoreFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnBean(SaveableEventStore.class)
    public ContractEventListener saveableEventStoreUpdater(SaveableEventStore eventStore) {
        return new SaveableEventStoreUpdater(eventStore);
    }
}
