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

import io.reactivex.disposables.Disposable;
import java.io.IOException;
import java.math.BigInteger;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.BlockchainException;
import net.consensys.eventeum.chain.service.block.BlockNumberService;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.wrapper.Web3jBlock;
import net.consensys.eventeum.service.AsyncTaskService;
import net.consensys.eventeum.utils.ExecutorNameFactory;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.websocket.events.NewHead;

@Slf4j
public class PubSubBlockSubscriptionStrategy extends AbstractBlockSubscriptionStrategy<NewHead> {

  private static final String PUB_SUB_EXECUTOR_NAME = "PUBSUB";
  private final AsyncTaskService asyncService;
  private RetryTemplate retryTemplate;

  public PubSubBlockSubscriptionStrategy(
      Web3j web3j,
      String nodeName,
      String nodeType,
      AsyncTaskService asyncService,
      BlockNumberService blockNumberService) {
    super(web3j, nodeName, nodeType, asyncService, blockNumberService);

    this.asyncService = asyncService;
  }

  @Override
  public Disposable subscribe() {
    final BigInteger startBlock = getStartBlock();
    final DefaultBlockParameter blockParam = DefaultBlockParameter.valueOf(startBlock);

    // New heads can only start from latest block so we need to obtain missing blocks first
    blockSubscription =
        web3j
            .replayPastBlocksFlowable(blockParam, true)
            .doOnComplete(() -> blockSubscription = subscribeToNewHeads())
            .subscribe(ethBlock -> triggerListeners(convertToEventeumBlock(ethBlock)));

    return blockSubscription;
  }

  private Disposable subscribeToNewHeads() {
    final Disposable disposable =
        web3j
            .newHeadsNotifications()
            .subscribe(
                newHead -> {
                  // Need to execute this is a seperate thread to workaround websocket thread
                  // deadlock
                  asyncService.execute(
                      ExecutorNameFactory.build(PUB_SUB_EXECUTOR_NAME, nodeName),
                      () -> triggerListeners(newHead.getParams().getResult()));
                });

    if (disposable.isDisposed()) {
      throw new BlockchainException(
          "Error when subscribing to newheads.  Disposable already disposed.");
    }

    return disposable;
  }

  NewHead convertToNewHead(EthBlock ethBlock) {
    final BasicNewHead newHead = new BasicNewHead();
    newHead.setHash(ethBlock.getBlock().getHash());
    newHead.setNumber(ethBlock.getBlock().getNumberRaw());
    newHead.setTimestamp(ethBlock.getBlock().getTimestampRaw());

    return newHead;
  }

  @Override
  Block convertToEventeumBlock(NewHead blockObject) {
    return new Web3jBlock(getEthBlock(blockObject.getHash()).getBlock(), nodeName);
  }

  Block convertToEventeumBlock(EthBlock blockObject) {
    return new Web3jBlock(blockObject.getBlock(), nodeName);
  }

  protected RetryTemplate getRetryTemplate() {
    if (retryTemplate == null) {
      retryTemplate = new RetryTemplate();

      final FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
      fixedBackOffPolicy.setBackOffPeriod(500);
      retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

      final SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
      retryPolicy.setMaxAttempts(10);
      retryTemplate.setRetryPolicy(retryPolicy);
    }

    return retryTemplate;
  }

  private EthBlock getEthBlock(String blockHash) {
    return getRetryTemplate()
        .execute(
            (context) -> {
              try {
                final EthBlock block = web3j.ethGetBlockByHash(blockHash, true).send();

                if (block == null || block.getBlock() == null) {
                  throw new BlockchainException(
                      String.format("Block not found. Hash: %s", blockHash));
                }

                return block;
              } catch (IOException e) {
                throw new BlockchainException("Unable to retrieve block details", e);
              }
            });
  }

  @Setter
  private class BasicNewHead extends NewHead {
    private String hash;

    private String number;

    private String timestamp;

    @Override
    public String getHash() {
      return hash;
    }

    @Override
    public String getNumber() {
      return number;
    }

    @Override
    public String getTimestamp() {
      return timestamp;
    }
  }
}
