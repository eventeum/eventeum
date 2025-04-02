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

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.config.EventFilterConfiguration;
import net.consensys.eventeum.chain.config.TransactionFilterConfiguration;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.factory.ContractEventFilterFactory;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import net.consensys.eventeum.repository.ContractEventFilterRepository;
import net.consensys.eventeum.repository.TransactionMonitoringSpecRepository;
import net.consensys.eventeum.service.SubscriptionService;
import net.consensys.eventeum.service.TransactionMonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ChainBootstrapperTest {

  private final List<BlockListener> mockBlockListeners =
      Arrays.asList(mock(BlockListener.class), mock(BlockListener.class));
  @Mock private BlockchainService mockBlockchainService;
  @Mock private EventFilterConfiguration mockConfig;
  @Mock private SubscriptionService mockSubscriptionService;
  @Mock private TransactionMonitoringService mockTransactionMonitoringService;
  @Mock private ContractEventFilterRepository mockFilterRepository;
  @Mock private TransactionMonitoringSpecRepository mockTransactionMonitoringRepository;
  @Mock private ContractEventFilterFactory mockFilterFactory;
  @Mock private TransactionFilterConfiguration transactionFilterConfiguration;
  private ChainBootstrapper underTest;

  @BeforeEach
  public void init() {
    underTest =
        new ChainBootstrapper(
            mockSubscriptionService,
            mockTransactionMonitoringService,
            mockConfig,
            mockFilterRepository,
            mockTransactionMonitoringRepository,
            Optional.of(Collections.singletonList(mockFilterFactory)),
            transactionFilterConfiguration);
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

    verify(mockSubscriptionService, times(1))
        .registerContractEventFilterWithRetries(mockConfiguredFilters.getFirst(), true);
    verify(mockSubscriptionService, times(1))
        .registerContractEventFilterWithRetries(mockConfiguredFilters.get(1), true);
    verify(mockSubscriptionService, times(1))
        .registerContractEventFilterWithRetries(mockFilterFactoryFilters.getFirst(), true);
    verify(mockSubscriptionService, times(1))
        .registerContractEventFilterWithRetries(mockFilterFactoryFilters.get(1), true);
  }

  @Test
  public void testThatAlreadyExistingEventFiltersAreRemoved() throws Exception {
    ContractEventFilter contractEventFilter = new ContractEventFilter();
    contractEventFilter.setId("id1");

    final List<ContractEventFilter> mockExistingEventFilters =
        Arrays.asList(contractEventFilter, mock(ContractEventFilter.class));
    final List<ContractEventFilter> mockConfiguredFilters =
        Collections.singletonList(contractEventFilter);
    final List<ContractEventFilter> mockFilterFactoryFilters =
        Arrays.asList(mock(ContractEventFilter.class), mock(ContractEventFilter.class));

    when(mockConfig.getConfiguredEventFilters()).thenReturn(mockConfiguredFilters);
    when(mockFilterRepository.findAll()).thenReturn(mockExistingEventFilters);
    when(mockFilterFactory.build()).thenReturn(mockFilterFactoryFilters);

    doBootstrap();

    verify(mockSubscriptionService, times(1))
        .registerContractEventFilterWithRetries(mockConfiguredFilters.getFirst(), true);
    verify(mockSubscriptionService, times(0))
        .registerContractEventFilterWithRetries(mockExistingEventFilters.getFirst(), false);
    verify(mockSubscriptionService, times(1))
        .registerContractEventFilterWithRetries(mockExistingEventFilters.get(1), false);
    verify(mockSubscriptionService, times(1))
        .registerContractEventFilterWithRetries(mockFilterFactoryFilters.getFirst(), true);
    verify(mockSubscriptionService, times(1))
        .registerContractEventFilterWithRetries(mockFilterFactoryFilters.get(1), true);
  }

  @Test
  public void testThatTransactionsMonitorsAreRegistered() throws Exception {

    final List<TransactionMonitoringSpec> mockMonitorSpecs =
        Arrays.asList(mock(TransactionMonitoringSpec.class), mock(TransactionMonitoringSpec.class));

    when(mockTransactionMonitoringRepository.findAll()).thenReturn(mockMonitorSpecs);

    doBootstrap();

    verify(mockTransactionMonitoringService, times(1))
        .registerTransactionsToMonitor(mockMonitorSpecs.getFirst(), true);
    verify(mockTransactionMonitoringService, times(1))
        .registerTransactionsToMonitor(mockMonitorSpecs.get(1), true);
  }

  @Test
  public void testThatContractTransactionFiltersAreRegistered() throws Exception {

    final List<TransactionMonitoringSpec> mockConfiguredFilters =
        Arrays.asList(mock(TransactionMonitoringSpec.class), mock(TransactionMonitoringSpec.class));

    when(transactionFilterConfiguration.getConfiguredTransactionFilters())
        .thenReturn(mockConfiguredFilters);

    doBootstrap();

    verify(mockTransactionMonitoringService, times(1))
        .registerTransactionsToMonitor(mockConfiguredFilters.getFirst(), true);
    verify(mockTransactionMonitoringService, times(1))
        .registerTransactionsToMonitor(mockConfiguredFilters.get(1), true);
  }

  private void doBootstrap() throws Exception {
    underTest.onApplicationEvent(null);
  }
}
