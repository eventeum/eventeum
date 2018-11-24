package net.consensys.eventeum.chain;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.factory.ContractEventFilterFactory;
import net.consensys.eventeum.repository.ContractEventFilterRepository;
import net.consensys.eventeum.service.SubscriptionService;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.config.EventFilterConfiguration;
import net.consensys.eventeum.chain.service.BlockchainService;
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
    private ContractEventFilterRepository mockFilterRepository;

    @Mock
    private ContractEventFilterFactory mockFilterFactory;

    private List<BlockListener> mockBlockListeners =
            Arrays.asList(mock(BlockListener.class), mock(BlockListener.class));

    private ChainBootstrapper underTest;

    @Before
    public void init() {
        underTest = new ChainBootstrapper(mockConfig,
                mockSubscriptionService, mockFilterRepository, Optional.of(Collections.singletonList(mockFilterFactory)));
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

    private void doBootstrap() throws Exception {
        underTest.afterPropertiesSet();
    }
}
