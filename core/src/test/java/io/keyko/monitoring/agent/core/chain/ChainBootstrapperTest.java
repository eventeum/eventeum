package io.keyko.monitoring.agent.core.chain;

import io.keyko.monitoring.agent.core.chain.config.TransactionFilterConfiguration;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import io.keyko.monitoring.agent.core.factory.ContractEventFilterFactory;
import io.keyko.monitoring.agent.core.model.TransactionMonitoringSpec;
import io.keyko.monitoring.agent.core.repository.ContractEventFilterRepository;
import io.keyko.monitoring.agent.core.repository.TransactionMonitoringSpecRepository;
import io.keyko.monitoring.agent.core.service.SubscriptionService;
import io.keyko.monitoring.agent.core.chain.block.BlockListener;
import io.keyko.monitoring.agent.core.chain.config.EventFilterConfiguration;
import io.keyko.monitoring.agent.core.chain.service.BlockchainService;
import io.keyko.monitoring.agent.core.service.TransactionMonitoringService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ChainBootstrapperTest {

    @Mock
    private BlockchainService mockBlockchainService;

    @Mock
    private EventFilterConfiguration mockConfig;

    @Mock
    private SubscriptionService mockSubscriptionService;

    @Mock
    private TransactionMonitoringService mockTransactionMonitoringService;

    @Mock
    private ContractEventFilterRepository mockFilterRepository;

    @Mock
    private TransactionMonitoringSpecRepository mockTransactionMonitoringRepository;

    @Mock
    private ContractEventFilterFactory mockFilterFactory;

    @Mock
    private TransactionFilterConfiguration transactionFilterConfiguration;


    private List<BlockListener> mockBlockListeners =
            Arrays.asList(mock(BlockListener.class), mock(BlockListener.class));

    private ChainBootstrapper underTest;

    @Before
    public void init() {
        underTest = new ChainBootstrapper(mockSubscriptionService, mockTransactionMonitoringService, mockConfig,
                mockFilterRepository, mockTransactionMonitoringRepository,
                Optional.of(Collections.singletonList(mockFilterFactory)), transactionFilterConfiguration);
    }

    @Test
    public void testThatEventFiltersAreRegistered() throws Exception {

        final List<ContractEventFilter> mockConfiguredFilters =
                Arrays.asList(mock(ContractEventFilter.class), mock(ContractEventFilter.class));
        final List<ContractEventFilter> mockFilterFactoryFilters =
                Arrays.asList(mock(ContractEventFilter.class), mock(ContractEventFilter.class));

        when(mockConfig.getConfiguredEventFilters()).thenReturn(mockConfiguredFilters);
        when(mockFilterFactory.build()).thenReturn(mockFilterFactoryFilters);

        doBootstrap();

        verify(mockSubscriptionService, times(1)).registerContractEventFilterWithRetries(mockConfiguredFilters.get(0), true);
        verify(mockSubscriptionService, times(1)).registerContractEventFilterWithRetries(mockConfiguredFilters.get(1), true);
        verify(mockSubscriptionService, times(1)).registerContractEventFilterWithRetries(mockFilterFactoryFilters.get(0), true);
        verify(mockSubscriptionService, times(1)).registerContractEventFilterWithRetries(mockFilterFactoryFilters.get(1), true);
    }

    @Test
    public void testThatTransactionsMonitorsAreRegistered() throws Exception {

        final List<TransactionMonitoringSpec> mockMonitorSpecs =
                Arrays.asList(mock(TransactionMonitoringSpec.class), mock(TransactionMonitoringSpec.class));

        when(mockTransactionMonitoringRepository.findAll()).thenReturn(mockMonitorSpecs);

        doBootstrap();

        verify(mockTransactionMonitoringService, times(1)).registerTransactionsToMonitor(mockMonitorSpecs.get(0), true);
        verify(mockTransactionMonitoringService, times(1)).registerTransactionsToMonitor(mockMonitorSpecs.get(1), true);
    }

    @Test
    public void testThatContractTransactionFiltersAreRegistered() throws Exception {

        final List<TransactionMonitoringSpec> mockConfiguredFilters =
                Arrays.asList(mock(TransactionMonitoringSpec.class), mock(TransactionMonitoringSpec.class));

        when(transactionFilterConfiguration.getConfiguredTransactionFilters()).thenReturn(mockConfiguredFilters);

        doBootstrap();

        verify(mockTransactionMonitoringService, times(1)).registerTransactionsToMonitor(mockConfiguredFilters.get(0), true);
        verify(mockTransactionMonitoringService, times(1)).registerTransactionsToMonitor(mockConfiguredFilters.get(1), true);
     }

    private void doBootstrap() throws Exception {
        underTest.afterPropertiesSet();
    }
}