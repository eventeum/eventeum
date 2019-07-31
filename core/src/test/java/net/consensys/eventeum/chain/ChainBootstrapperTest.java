package net.consensys.eventeum.chain;

import net.consensys.eventeum.chain.config.TransactionFilterConfiguration;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.factory.ContractEventFilterFactory;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import net.consensys.eventeum.repository.ContractEventFilterRepository;
import net.consensys.eventeum.repository.TransactionMonitoringSpecRepository;
import net.consensys.eventeum.service.SubscriptionService;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.config.EventFilterConfiguration;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.service.TransactionMonitoringService;
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

        verify(mockSubscriptionService, times(1)).registerContractEventFilter(mockConfiguredFilters.get(0), true);
        verify(mockSubscriptionService, times(1)).registerContractEventFilter(mockConfiguredFilters.get(1), true);
        verify(mockSubscriptionService, times(1)).registerContractEventFilter(mockFilterFactoryFilters.get(0), true);
        verify(mockSubscriptionService, times(1)).registerContractEventFilter(mockFilterFactoryFilters.get(1), true);
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
