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

package net.consensys.eventeum.chain;

import lombok.AllArgsConstructor;
import net.consensys.eventeum.chain.config.TransactionFilterConfiguration;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.factory.ContractEventFilterFactory;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import net.consensys.eventeum.service.SubscriptionService;
import net.consensys.eventeum.chain.config.EventFilterConfiguration;
import net.consensys.eventeum.service.TransactionMonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Registers filters that are either configured within the properties file, exist in the
 * Eventeum database on startup, or are returned from ContractEventFilterFactory beans.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Service
@AllArgsConstructor
public class ChainBootstrapper {
    private final Logger LOG = LoggerFactory.getLogger(ChainBootstrapper.class);

    private SubscriptionService subscriptionService;
    private TransactionMonitoringService transactionMonitoringService;
    private EventFilterConfiguration filterConfiguration;
    private CrudRepository<ContractEventFilter, String> filterRepository;
    private CrudRepository<TransactionMonitoringSpec, String> transactionMonitoringRepository;
    private Optional<List<ContractEventFilterFactory>> contractEventFilterFactories;
    private TransactionFilterConfiguration transactionFilterConfiguration;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        registerTransactionsToMonitor(transactionMonitoringRepository.findAll(), true);
        registerTransactionsToMonitor(transactionFilterConfiguration.getConfiguredTransactionFilters(), true);

        subscriptionService.init(filterConfiguration.getConfiguredEventFilters());
        registerFilters(filterConfiguration.getConfiguredEventFilters(), true);
        registerFilters(filterRepository.findAll(), false);

        contractEventFilterFactories.ifPresent((factories) -> {
            factories.forEach(factory -> registerFilters(factory.build(), true));
        });
    }

    private void registerFilters(Iterable<ContractEventFilter> filters, boolean broadcast) {
        if (filters != null) {
            filters.forEach(filter -> registerFilter(filter, broadcast));
        }
    }

    private void registerFilter(ContractEventFilter filter, boolean broadcast) {
        subscriptionService.registerContractEventFilterWithRetries(filter, broadcast);
    }

    private void registerTransactionsToMonitor(Iterable<TransactionMonitoringSpec> specs, boolean broadcast) {
        if (specs != null) {
            specs.forEach(spec -> registerTransactionToMonitor(spec, broadcast));
        }
    }

    private void registerTransactionToMonitor(TransactionMonitoringSpec spec, boolean broadcast) {
        transactionMonitoringService.registerTransactionsToMonitor(spec, broadcast);
    }
}
