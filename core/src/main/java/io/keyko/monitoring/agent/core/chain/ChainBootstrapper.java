package io.keyko.monitoring.agent.core.chain;

import io.keyko.monitoring.agent.core.model.TransactionMonitoringSpec;
import lombok.AllArgsConstructor;
import io.keyko.monitoring.agent.core.chain.config.EventFilterConfiguration;
import io.keyko.monitoring.agent.core.chain.config.TransactionFilterConfiguration;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import io.keyko.monitoring.agent.core.factory.ContractEventFilterFactory;
import io.keyko.monitoring.agent.core.service.SubscriptionService;
import io.keyko.monitoring.agent.core.service.TransactionMonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
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
public class ChainBootstrapper implements InitializingBean {
    private final Logger LOG = LoggerFactory.getLogger(ChainBootstrapper.class);

    private SubscriptionService subscriptionService;
    private TransactionMonitoringService transactionMonitoringService;
    private EventFilterConfiguration filterConfiguration;
    private CrudRepository<ContractEventFilter, String> filterRepository;
    private CrudRepository<TransactionMonitoringSpec, String> transactionMonitoringRepository;
    private Optional<List<ContractEventFilterFactory>> contractEventFilterFactories;
    private TransactionFilterConfiguration transactionFilterConfiguration;

    @Override
    public void afterPropertiesSet() throws Exception {
        registerTransactionsToMonitor(transactionMonitoringRepository.findAll(), true);
        registerTransactionsToMonitor(transactionFilterConfiguration.getConfiguredTransactionFilters(), true);

        subscriptionService.init();
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
