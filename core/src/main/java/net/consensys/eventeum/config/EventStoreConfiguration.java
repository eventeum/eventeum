/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.consensys.eventeum.config;

import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.block.EventStoreLatestBlockUpdater;
import net.consensys.eventeum.chain.contract.ContractEventListener;
import net.consensys.eventeum.chain.contract.EventStoreContractEventUpdater;
import net.consensys.eventeum.chain.factory.BlockDetailsFactory;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.factory.EventStoreFactory;
import net.consensys.eventeum.integration.eventstore.EventStore;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
import net.consensys.eventeum.integration.eventstore.db.MongoEventStore;
import net.consensys.eventeum.integration.eventstore.db.SqlEventStore;
import net.consensys.eventeum.integration.eventstore.db.repository.ContractEventDetailsRepository;
import net.consensys.eventeum.integration.eventstore.db.repository.LatestBlockRepository;
import net.consensys.eventeum.integration.eventstore.db.repository.MessageDetailsRepository;
import net.consensys.eventeum.integration.eventstore.rest.RESTEventStore;
import net.consensys.eventeum.integration.eventstore.rest.client.EventStoreClient;
import net.consensys.eventeum.monitoring.EventeumValueMonitor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
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
  @ConditionalOnExpression("'${eventStore.type}:${database.type}'=='DB:MONGO'")
  @ConditionalOnMissingBean(EventStoreFactory.class)
  public static class MongoEventStoreConfiguration {

    @Bean
    public SaveableEventStore dbEventStore(
        ContractEventDetailsRepository contractEventRepository,
        MessageDetailsRepository messageDetailsRepository,
        LatestBlockRepository latestBlockRepository,
        MongoTemplate mongoTemplate) {
      return new MongoEventStore(
          contractEventRepository, messageDetailsRepository, latestBlockRepository, mongoTemplate);
    }

    @Bean
    public ContractEventListener eventStoreContractEventUpdater(SaveableEventStore eventStore) {
      return new EventStoreContractEventUpdater(eventStore);
    }

    @Bean
    public BlockListener eventStoreLatestBlockUpdater(
        SaveableEventStore eventStore,
        BlockDetailsFactory blockDetailsFactory,
        EventeumValueMonitor valueMonitor,
        ChainServicesContainer chainServicesContainer) {
      return new EventStoreLatestBlockUpdater(
          eventStore, blockDetailsFactory, valueMonitor, chainServicesContainer);
    }
  }

  @Configuration
  @ConditionalOnExpression("'${eventStore.type}:${database.type}'=='DB:SQL'")
  @ConditionalOnMissingBean(EventStoreFactory.class)
  public static class SqlEventStoreConfiguration {

    @Bean
    public SaveableEventStore dbEventStore(
        ContractEventDetailsRepository contractEventRepository,
        MessageDetailsRepository messageDetailsRepository,
        LatestBlockRepository latestBlockRepository) {
      return new SqlEventStore(
          contractEventRepository, messageDetailsRepository, latestBlockRepository);
    }

    @Bean
    public ContractEventListener eventStoreContractEventUpdater(SaveableEventStore eventStore) {
      return new EventStoreContractEventUpdater(eventStore);
    }

    @Bean
    public BlockListener eventStoreLatestBlockUpdater(
        SaveableEventStore eventStore,
        BlockDetailsFactory blockDetailsFactory,
        EventeumValueMonitor valueMonitor,
        ChainServicesContainer chainServiceContainer) {
      return new EventStoreLatestBlockUpdater(
          eventStore, blockDetailsFactory, valueMonitor, chainServiceContainer);
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
