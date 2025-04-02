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

package net.consensys.eventeum.chain.service.strategy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.service.HederaService;
import net.consensys.eventeum.chain.service.block.BlockNumberService;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.wrapper.HederaBlock;
import net.consensys.eventeum.chain.settings.NodeType;
import net.consensys.eventeum.settings.EventeumSettings;
import net.consensys.eventeum.testutils.DummyAsyncTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;

@Slf4j
public class PollingBlockchainSubscriptionStrategyTest {

  private static final String BLOCK_HASH =
      "0xc0e07697167c58f2a173df45f5c9b2c46ca0941cdf0bf79616d53dc92f62aebd";

  private static final BigInteger BLOCK_NUMBER = BigInteger.valueOf(1);

  private static final BigInteger BLOCK_TIMESTAMP = BigInteger.valueOf(12345678);

  private static final Long POLLING_INTERVAL = 1000L;

  private static final String NODE_NAME = "mainnet";

  private PollingBlockSubscriptionStrategy underTest;

  private PublishProcessor<EthBlock> blockPublishProcessor;

  private PublishProcessor<HederaBlock> hederaBlockPublishProcessor;

  private Web3j mockWeb3j;

  private EthBlock mockEthBlock;

  private BlockListener mockBlockListener;

  private BlockNumberService mockBlockNumberService;

  private EventeumSettings mockSettings;
  private HederaService mockHederaService;

  @BeforeEach
  public void init() {
    this.mockWeb3j = mock(Web3j.class);

    mockEthBlock = mock(EthBlock.class);
    mockBlockNumberService = mock(BlockNumberService.class);
    mockSettings = mock(EventeumSettings.class);
    mockHederaService = mock(HederaService.class);
    final EthBlock.Block mockBlock = mock(EthBlock.Block.class);

    when(mockBlock.getNumber()).thenReturn(BLOCK_NUMBER);
    when(mockBlock.getHash()).thenReturn(BLOCK_HASH);
    when(mockBlock.getTimestamp()).thenReturn(BLOCK_TIMESTAMP);
    when(mockEthBlock.getBlock()).thenReturn(mockBlock);

    hederaBlockPublishProcessor = PublishProcessor.create();
    when(mockHederaService.blocksFlowable(BLOCK_NUMBER, POLLING_INTERVAL))
        .thenReturn(hederaBlockPublishProcessor);

    blockPublishProcessor = PublishProcessor.create();
    when(mockWeb3j.replayPastAndFutureBlocksFlowable(any(), eq(true)))
        .thenReturn(blockPublishProcessor);

    when(mockBlockNumberService.getStartBlockForNode(NODE_NAME)).thenReturn(BigInteger.ONE);

    underTest =
        new PollingBlockSubscriptionStrategy(
            mockWeb3j,
            NODE_NAME,
            NodeType.NORMAL.name(),
            new DummyAsyncTaskService(),
            mockBlockNumberService,
            mockHederaService,
            POLLING_INTERVAL);
  }

  @Test
  public void testEthSubscribe() {
    final Disposable returnedSubscription = underTest.subscribe();

    assertFalse(returnedSubscription.isDisposed());
  }

  @Test
  public void testHederaSubscribe() {
    underTest =
        new PollingBlockSubscriptionStrategy(
            mockWeb3j,
            NODE_NAME,
            NodeType.MIRROR.name(),
            new DummyAsyncTaskService(),
            mockBlockNumberService,
            mockHederaService,
            POLLING_INTERVAL);

    final Disposable returnedSubscription = underTest.subscribe();

    assertFalse(returnedSubscription.isDisposed());
  }

  @Test
  public void testUnsubscribe() {
    final Disposable returnedSubscription = underTest.subscribe();

    assertFalse(returnedSubscription.isDisposed());

    underTest.unsubscribe();

    assertTrue(returnedSubscription.isDisposed());
  }

  @Test
  public void testAddBlockListener() {
    underTest.subscribe();
    final Block block = doRegisterBlockListenerAndTrigger();
    assertNotNull(block);
  }

  @Test
  public void testRemoveBlockListener() {
    underTest.subscribe();
    final Block block = doRegisterBlockListenerAndTrigger();
    assertNotNull(block);

    reset(mockBlockListener);
    underTest.removeBlockListener(mockBlockListener);

    blockPublishProcessor.onNext(mockEthBlock);

    verify(mockBlockListener, never()).onBlock(any());
  }

  @Test
  public void testBlockHashPassedToListenerIsCorrect() {
    underTest.subscribe();
    final Block block = doRegisterBlockListenerAndTrigger();

    assertEquals(BLOCK_HASH, block.getHash());
  }

  @Test
  public void testBlockNumberPassedToListenerIsCorrect() {
    underTest.subscribe();
    final Block block = doRegisterBlockListenerAndTrigger();

    assertEquals(BLOCK_NUMBER, block.getNumber());
  }

  @Test
  public void testBlockTimestampPassedToListenerIsCorrect() {
    underTest.subscribe();
    final Block block = doRegisterBlockListenerAndTrigger();

    assertEquals(BLOCK_TIMESTAMP, block.getTimestamp());
  }

  @Test
  public void testBlockNodeNamePassedToListenerIsCorrect() {
    underTest.subscribe();
    final Block block = doRegisterBlockListenerAndTrigger();

    assertEquals(NODE_NAME, block.getNodeName());
  }

  private Block doRegisterBlockListenerAndTrigger() {

    mockBlockListener = mock(BlockListener.class);
    underTest.addBlockListener(mockBlockListener);

    blockPublishProcessor.onNext(mockEthBlock);

    final ArgumentCaptor<Block> captor = ArgumentCaptor.forClass(Block.class);
    try {
      TimeUnit.SECONDS.sleep(8);
    } catch (Exception ignore) {

    }

    verify(mockBlockListener).onBlock(captor.capture());

    return captor.getValue();
  }
}
