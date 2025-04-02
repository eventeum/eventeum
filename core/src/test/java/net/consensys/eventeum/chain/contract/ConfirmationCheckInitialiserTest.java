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

package net.consensys.eventeum.chain.contract;

import static org.mockito.Mockito.*;

import java.math.BigInteger;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.container.NodeServices;
import net.consensys.eventeum.chain.service.domain.TransactionReceipt;
import net.consensys.eventeum.chain.service.strategy.BlockSubscriptionStrategy;
import net.consensys.eventeum.chain.settings.ChainType;
import net.consensys.eventeum.chain.settings.Node;
import net.consensys.eventeum.chain.settings.NodeSettings;
import net.consensys.eventeum.constant.Constants;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.integration.eventstore.EventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConfirmationCheckInitialiserTest {

  private static final String TX_HASH =
      "0x05ba7cdf9f35579c9e2332804a3a98bf2231572e8bfe57b3e31ed0240ae7f582";
  private static final String BLOCK_HASH =
      "0xb9f2b107229b1f49547a7d0d446d018adef30b83ae8a69738c2f38375b28f4dc";
  private final BigInteger currentBlock = BigInteger.valueOf(2000);
  private BroadcastAndInitialiseConfirmationListener underTest;
  private BlockchainService mockBlockchainService;
  private BlockSubscriptionStrategy mockBlockSubscriptionStrategy;
  private BlockListener mockBlockListener;

  @BeforeEach
  public void init() {

    mockBlockchainService = mock(BlockchainService.class);
    mockBlockSubscriptionStrategy = mock(BlockSubscriptionStrategy.class);
    mockBlockListener = mock(BlockListener.class);
    ChainServicesContainer mockChainServicesContainer = mock(ChainServicesContainer.class);
    NodeServices mockNodeServices = mock(NodeServices.class);
    NodeSettings mockNodeSettings = mock(NodeSettings.class);
    EventStore mockEventStore = mock(EventStore.class);

    when(mockChainServicesContainer.getNodeServices(Constants.DEFAULT_NODE_NAME))
        .thenReturn(mockNodeServices);

    when(mockNodeServices.getBlockchainService()).thenReturn(mockBlockchainService);
    when(mockNodeServices.getBlockSubscriptionStrategy()).thenReturn(mockBlockSubscriptionStrategy);
    when(mockBlockchainService.getCurrentBlockNumber()).thenReturn(currentBlock);
    Node node = new Node();
    node.setChainType(ChainType.ETHEREUM);
    node.setBlocksToWaitForConfirmation(BigInteger.valueOf(10));
    node.setBlocksToWaitForMissingTx(BigInteger.valueOf(100));
    node.setBlocksToWaitBeforeInvalidating(BigInteger.valueOf(5));
    when(mockNodeSettings.getNode(any())).thenReturn(node);

    underTest =
        new ConfirmationCheckInitialiserForTest(
            mockChainServicesContainer,
            mock(BlockchainEventBroadcaster.class),
            mockNodeSettings,
            mockEventStore);
  }

  @Test
  public void testOnEventNotInvalidated() {
    ContractEventDetails event = createContractEventDetails(ContractEventStatus.UNCONFIRMED);
    when(event.getBlockNumber()).thenReturn(currentBlock);
    underTest.onEvent(event);

    verify(mockBlockSubscriptionStrategy, times(1)).addBlockListener(mockBlockListener);
  }

  @Test
  public void testOnEventInvalidated() {
    underTest.onEvent(createContractEventDetails(ContractEventStatus.INVALIDATED));

    verify(mockBlockSubscriptionStrategy, never()).addBlockListener(mockBlockListener);
  }

  @Test
  public void testOnEventWithAExpiredBlockEvent() {
    ContractEventDetails event = createContractEventDetails(ContractEventStatus.UNCONFIRMED);
    when(event.getBlockNumber()).thenReturn(BigInteger.valueOf(1));

    final TransactionReceipt mockTxReceipt = mock(TransactionReceipt.class);
    when(mockTxReceipt.getBlockHash()).thenReturn(BLOCK_HASH);

    when(mockBlockchainService.getTransactionReceipt(TX_HASH)).thenReturn(mockTxReceipt);
    underTest.onEvent(event);

    verify(mockBlockSubscriptionStrategy, times(0)).addBlockListener(mockBlockListener);
  }

  private ContractEventDetails createContractEventDetails(ContractEventStatus status) {
    final ContractEventDetails eventDetails = mock(ContractEventDetails.class);

    when(eventDetails.getStatus()).thenReturn(status);
    when(eventDetails.getNodeName()).thenReturn(Constants.DEFAULT_NODE_NAME);
    when(eventDetails.getBlockNumber()).thenReturn(BigInteger.valueOf(0));
    when(eventDetails.getBlockHash()).thenReturn(BLOCK_HASH);
    when(eventDetails.getTransactionHash()).thenReturn(TX_HASH);

    return eventDetails;
  }

  private class ConfirmationCheckInitialiserForTest
      extends BroadcastAndInitialiseConfirmationListener {

    public ConfirmationCheckInitialiserForTest(
        ChainServicesContainer chainServicesContainer,
        BlockchainEventBroadcaster eventBroadcaster,
        NodeSettings node,
        EventStore eventStore) {
      super(chainServicesContainer, eventBroadcaster, node, eventStore);
    }

    @Override
    protected BlockListener createEventConfirmationBlockListener(
        ContractEventDetails eventDetails, Node node) {
      return mockBlockListener;
    }
  }
}
