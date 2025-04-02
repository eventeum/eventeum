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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;
import java.io.IOException;
import java.math.BigInteger;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.service.block.BlockNumberService;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.settings.NodeType;
import net.consensys.eventeum.testutils.DummyAsyncTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.websocket.events.NewHead;
import org.web3j.protocol.websocket.events.NewHeadsNotification;
import org.web3j.protocol.websocket.events.NotificationParams;
import org.web3j.utils.Numeric;

public class PubSubBlockchainSubscriptionStrategyTest {

  private static final String BLOCK_HASH =
      "0xc0e07697167c58f2a173df45f5c9b2c46ca0941cdf0bf79616d53dc92f62aebd";

  private static final BigInteger BLOCK_NUMBER = BigInteger.valueOf(123);

  private static final String BLOCK_NUMBER_HEX = "0x7B";

  // 12345678
  private static final String BLOCK_TIMESTAMP = "0xbc614e";

  private static final String NODE_NAME = "mainnet";

  private PubSubBlockSubscriptionStrategy underTest;

  private PublishProcessor<NewHeadsNotification> blockPublishProcessor;

  private Web3j mockWeb3j;

  private NewHeadsNotification mockNewHeadsNotification;

  private NewHead mockNewHead;

  private EthBlock mockEthBlock;

  private BlockListener mockBlockListener;

  private BlockNumberService mockBlockNumberService;

  @BeforeEach
  public void init() throws IOException {
    this.mockWeb3j = mock(Web3j.class);

    mockNewHeadsNotification = mock(NewHeadsNotification.class);
    mockBlockNumberService = mock(BlockNumberService.class);
    when(mockNewHeadsNotification.getParams()).thenReturn(new NewHeadNotificationParameter());

    mockNewHead = mock(NewHead.class);
    when(mockNewHead.getHash()).thenReturn(BLOCK_HASH);

    blockPublishProcessor = PublishProcessor.create();
    when(mockWeb3j.replayPastBlocksFlowable(any(), eq(true))).thenReturn(Flowable.empty());
    when(mockWeb3j.newHeadsNotifications()).thenReturn(blockPublishProcessor);

    mockEthBlock = mock(EthBlock.class);
    final EthBlock.Block mockBlock = mock(EthBlock.Block.class);

    when(mockBlock.getNumber()).thenReturn(BLOCK_NUMBER);
    when(mockBlock.getHash()).thenReturn(BLOCK_HASH);
    when(mockBlock.getTimestamp()).thenReturn(Numeric.toBigInt(BLOCK_TIMESTAMP));
    when(mockEthBlock.getBlock()).thenReturn(mockBlock);

    final Request<?, EthBlock> mockRequest = mock(Request.class);
    doReturn(mockRequest).when(mockWeb3j).ethGetBlockByHash(BLOCK_HASH, true);

    when(mockRequest.send()).thenReturn(mockEthBlock);

    when(mockBlockNumberService.getStartBlockForNode(NODE_NAME)).thenReturn(BigInteger.ONE);

    underTest =
        new PubSubBlockSubscriptionStrategy(
            mockWeb3j,
            NODE_NAME,
            NodeType.NORMAL.getNodeName(),
            new DummyAsyncTaskService(),
            mockBlockNumberService);
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

    blockPublishProcessor.onNext(mockNewHeadsNotification);

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

    assertEquals(BigInteger.valueOf(12345678), block.getTimestamp());
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

    blockPublishProcessor.onNext(mockNewHeadsNotification);

    final ArgumentCaptor<Block> captor = ArgumentCaptor.forClass(Block.class);
    verify(mockBlockListener).onBlock(captor.capture());

    return captor.getValue();
  }

  private class NewHeadNotificationParameter extends NotificationParams<NewHead> {
    @Override
    public NewHead getResult() {
      return mockNewHead;
    }
  }
}
