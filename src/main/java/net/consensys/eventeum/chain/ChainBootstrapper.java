package net.consensys.eventeum.chain;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.repository.ContractEventFilterRepository;
import net.consensys.eventeum.service.SubscriptionService;
import net.consensys.eventeum.chain.config.EventFilterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Registers filters that are either configured within the properties file, or exist in the
 * Eventeum database on startup.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Service
public class ChainBootstrapper implements InitializingBean {
    private final Logger LOG = LoggerFactory.getLogger(ChainBootstrapper.class);

    private SubscriptionService subscriptionService;
    private EventFilterConfiguration filterConfiguration;
    private ContractEventFilterRepository filterRepository;

    @Autowired
    public ChainBootstrapper(EventFilterConfiguration filterConfiguration,
                             SubscriptionService subscriptionService,
                             ContractEventFilterRepository filterRepository) {
        this.filterConfiguration = filterConfiguration;
        this.subscriptionService = subscriptionService;
        this.filterRepository = filterRepository;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        registerFilters(filterConfiguration.getConfiguredEventFilters(), true);
        registerFilters(filterRepository.findAll(), false);
    }

    private void registerFilters(List<ContractEventFilter> filters, boolean broadcast) {
        if (filters != null) {
            filters.forEach(filter -> subscriptionService.registerContractEventFilter(filter, broadcast));
        }
    }
}

