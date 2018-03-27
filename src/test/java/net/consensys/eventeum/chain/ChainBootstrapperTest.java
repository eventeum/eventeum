package net.consensys.eventeum.chain;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
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
import java.util.List;

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

    private List<BlockListener> mockBlockListeners =
            Arrays.asList(mock(BlockListener.class), mock(BlockListener.class));

    private ChainBootstrapper underTest;

    @Before
    public void init() {
        underTest = new ChainBootstrapper(mockConfig,
                mockSubscriptionService, mockFilterRepository);
    }

    @Test
    public void testThatEventFiltersAreRegistered() throws Exception {

        final List<ContractEventFilter> mockEventFilters = Arrays.asList(mock(ContractEventFilter.class), mock(ContractEventFilter.class));

        when(mockConfig.getConfiguredEventFilters()).thenReturn(mockEventFilters);

        doBootstrap();

        verify(mockSubscriptionService, times(1)).registerContractEventFilter(mockEventFilters.get(0), true);
        verify(mockSubscriptionService, times(1)).registerContractEventFilter(mockEventFilters.get(1), true);
    }

    private void doBootstrap() throws Exception {
        underTest.afterPropertiesSet();
    }
}
