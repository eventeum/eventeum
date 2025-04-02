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

package net.consensys.eventeum.chain.block;

import static org.mockito.Mockito.*;

import java.math.BigInteger;
import java.util.Arrays;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.Log;
import net.consensys.eventeum.chain.service.domain.TransactionReceipt;
import net.consensys.eventeum.chain.service.strategy.BlockSubscriptionStrategy;
import net.consensys.eventeum.chain.settings.Node;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EventConfirmationBlockListenerTest {

  private static final BigInteger BLOCKS_TO_WAIT = BigInteger.valueOf(10);
  private static final BigInteger BLOCKS_TO_WAIT_MISSING = BigInteger.valueOf(100);
  private static final BigInteger BLOCKS_TO_WAIT_BEFORE_INVALIDATING = BigInteger.valueOf(1);
  private static final String EVENT_BLOCK_HASH =
      "0x368ce0ee3afdf1bd73d7e6912f899f31b14b9656e1a3164400ba4587df192c1d";
  private static final BigInteger EVENT_BLOCK_NUMBER = BigInteger.valueOf(1000);
  private static final String EVENT_TX_HASH =
      "0x1fdb6a20587d7114ee471f3ec9d2517b267fc951eafc91ccdede7c50962a755b";
  private static final BigInteger EVENT_LOG_INDEX = BigInteger.valueOf(1);

  private EventConfirmationBlockListener underTest;

  private ContractEventDetails mockEventDetails;
  private BlockchainService mockBlockchainService;
  private BlockSubscriptionStrategy mockBlockSubscriptionStrategy;
  private BlockchainEventBroadcaster mockEventBroadcaster;
  private TransactionReceipt mockTransactionReceipt;
  private Log mockLog;

  @BeforeEach
  public void init() {
    mockEventDetails = mock(ContractEventDetails.class);
    mockBlockchainService = mock(BlockchainService.class);
    mockBlockSubscriptionStrategy = mock(BlockSubscriptionStrategy.class);
    mockEventBroadcaster = mock(BlockchainEventBroadcaster.class);
    mockTransactionReceipt = mock(TransactionReceipt.class);
    mockLog = mock(Log.class);

    final Log anotherMockLog = mock(Log.class);
    when(anotherMockLog.getLogIndex()).thenReturn(EVENT_LOG_INDEX.add(BigInteger.ONE));

    when(mockTransactionReceipt.getLogs()).thenReturn(Arrays.asList(anotherMockLog, mockLog));
    when(mockTransactionReceipt.getBlockHash()).thenReturn(EVENT_BLOCK_HASH);

    when(mockEventDetails.getBlockNumber()).thenReturn(EVENT_BLOCK_NUMBER);
    when(mockEventDetails.getTransactionHash()).thenReturn(EVENT_TX_HASH);
    when(mockEventDetails.getLogIndex()).thenReturn(EVENT_LOG_INDEX);
    when(mockEventDetails.getBlockHash()).thenReturn(EVENT_BLOCK_HASH);

    when(mockBlockchainService.getCurrentBlockNumber()).thenReturn(EVENT_BLOCK_NUMBER);
    when(mockBlockchainService.getTransactionReceipt(EVENT_TX_HASH))
        .thenReturn(mockTransactionReceipt);

    Node node = new Node();
    node.setBlocksToWaitForConfirmation(BLOCKS_TO_WAIT);
    node.setBlocksToWaitForMissingTx(BLOCKS_TO_WAIT_MISSING);
    node.setBlocksToWaitBeforeInvalidating(BLOCKS_TO_WAIT_BEFORE_INVALIDATING);

    underTest =
        new EventConfirmationBlockListener(
            mockEventDetails,
            mockBlockchainService,
            mockBlockSubscriptionStrategy,
            mockEventBroadcaster,
            node);
  }

  @Test
  public void testOnBlockWhenUnderBlockThresholdNoOrphan() {
    wireLog();
    underTest.onBlock(createBlockDetails(1002));

    expectNoBroadcast();
  }

  @Test
  public void testOnBlockWhenUnderBlockThresholdLogRemoved() {
    wireLog(true, EVENT_BLOCK_HASH, EVENT_LOG_INDEX);
    underTest.onBlock(createBlockDetails(1002));
    underTest.onBlock(createBlockDetails(1003));
    underTest.onBlock(createBlockDetails(1004));

    expectInvalidation();
  }

  @Test
  public void testOnBlockWhenUnderBlockThresholdBlockHashChanged() {
    wireLog(true, EVENT_BLOCK_HASH + "changed", EVENT_LOG_INDEX);
    underTest.onBlock(createBlockDetails(1002));
    underTest.onBlock(createBlockDetails(1003));
    underTest.onBlock(createBlockDetails(1004));

    expectInvalidation();
  }

  @Test
  public void testOnBlockWhenUnderBlockThresholdNoMatchingLog() {
    wireLog(true, EVENT_BLOCK_HASH, EVENT_LOG_INDEX.add(BigInteger.ONE));
    underTest.onBlock(createBlockDetails(1002));
    underTest.onBlock(createBlockDetails(1003));
    underTest.onBlock(createBlockDetails(1004));

    expectInvalidation();
  }

  @Test
  public void testOnBlockWhenOverBlockThreshold() {
    wireLog();
    underTest.onBlock(createBlockDetails(1011));

    expectBroadcastWithStatus(ContractEventStatus.CONFIRMED);
  }

  @Test
  public void testTransactionDoesntExistUnderThreshold() {
    when(mockBlockchainService.getTransactionReceipt(EVENT_TX_HASH)).thenReturn(null);

    underTest.onBlock(createBlockDetails(1005));
    underTest.onBlock(createBlockDetails(1103));

    expectNoBroadcast();
  }

  @Test
  public void testTransactionDoesntExistOverThreshold() {
    when(mockBlockchainService.getTransactionReceipt(EVENT_TX_HASH)).thenReturn(null);

    underTest.onBlock(createBlockDetails(1005));
    underTest.onBlock(createBlockDetails(1106));

    expectInvalidation();
  }

  private Block createBlockDetails(int blockNumber) {
    final Block block = mock(Block.class);
    when(block.getNumber()).thenReturn(BigInteger.valueOf(blockNumber));

    return block;
  }

  private void wireLog() {
    wireLog(false, EVENT_BLOCK_HASH, EVENT_LOG_INDEX);
  }

  private void wireLog(boolean isRemoved, String blockHash, BigInteger logIndex) {
    when(mockLog.isRemoved()).thenReturn(isRemoved);
    when(mockLog.getBlockHash()).thenReturn(blockHash);
    when(mockLog.getLogIndex()).thenReturn(logIndex);
  }

  private void expectNoBroadcast() {
    verify(mockEventBroadcaster, never()).broadcastContractEvent(any(ContractEventDetails.class));
  }

  private void expectInvalidation() {
    expectBroadcastWithStatus(ContractEventStatus.INVALIDATED);

    verify(mockBlockSubscriptionStrategy, times(1)).removeBlockListener(underTest);
  }

  private void expectBroadcastWithStatus(ContractEventStatus status) {
    verify(mockEventDetails, times(1)).setStatus(status);
    verify(mockEventBroadcaster, times(1)).broadcastContractEvent(mockEventDetails);
  }
}
