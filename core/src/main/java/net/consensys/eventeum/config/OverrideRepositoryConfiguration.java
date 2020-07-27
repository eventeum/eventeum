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

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.model.EventFilterSyncStatus;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import net.consensys.eventeum.repository.factory.ContractEventFilterRepositoryFactory;
import net.consensys.eventeum.repository.factory.EventFilterSyncStatusRepositoryFactory;
import net.consensys.eventeum.repository.factory.TransactionMonitoringSpecRepositoryFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.CrudRepository;

@Configuration
public class OverrideRepositoryConfiguration {

    @Bean
    @ConditionalOnBean(ContractEventFilterRepositoryFactory.class)
    public CrudRepository<ContractEventFilter, String> customContractEventFilterRepository(
            ContractEventFilterRepositoryFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnBean(EventFilterSyncStatusRepositoryFactory.class)
    public CrudRepository<EventFilterSyncStatus, String> customEventFilterSyncStatusRepository(
            EventFilterSyncStatusRepositoryFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnBean(TransactionMonitoringSpecRepositoryFactory.class)
    public CrudRepository<TransactionMonitoringSpec, String> customTransactionMonitoringSpecRepository(
            TransactionMonitoringSpecRepositoryFactory factory) {
        return factory.build();
    }
}
