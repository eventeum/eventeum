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
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.service.block.BlockNumberService;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.wrapper.Web3jBlock;
import net.consensys.eventeum.service.AsyncTaskService;
import net.consensys.eventeum.utils.ExecutorNameFactory;
import net.consensys.eventeum.utils.JSON;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public abstract class AbstractBlockSubscriptionStrategy<T> implements BlockSubscriptionStrategy {

    protected static final String BLOCK_EXECUTOR_NAME = "BLOCK";

    protected Collection<BlockListener> blockListeners = new ConcurrentLinkedQueue<>();
    protected Disposable blockSubscription;
    protected Web3j web3j;
    protected String nodeName;
    protected AsyncTaskService asyncService;
    protected BlockNumberService blockNumberService;
    protected AtomicLong lastBlockNumberProcessed = new AtomicLong(0);

    private AtomicBoolean errored = new AtomicBoolean(false);

    public AbstractBlockSubscriptionStrategy(Web3j web3j,
                                             String nodeName,
                                             AsyncTaskService asyncService,
                                             BlockNumberService blockNumberService) {
        this.web3j = web3j;
        this.nodeName = nodeName;
        this.asyncService = asyncService;
        this.blockNumberService = blockNumberService;
    }

    @Override
    public String getNodeName() {
        return nodeName;
    }

    @PreDestroy
    @Override
    public void unsubscribe() {
        try {
            if (blockSubscription != null) {
                blockSubscription.dispose();
            }
        } finally {
            blockSubscription = null;
            errored.set(false);
        }
    }

    @Override
    public void addBlockListener(BlockListener blockListener) {
        blockListeners.add(blockListener);
    }

    @Override
    public void removeBlockListener(BlockListener blockListener) {
        blockListeners.remove(blockListener);
    }

    public boolean isSubscribed() {
        return blockSubscription != null && !blockSubscription.isDisposed();
    }

    protected void triggerListeners(T blockObject) {
        final Block eventeumBlock = convertToEventeumBlock(blockObject);

        if (eventeumBlock != null) {
            triggerListeners(eventeumBlock);
        }
    }

    protected void triggerListeners(Block eventeumBlock) {
        asyncService.execute(ExecutorNameFactory.build(BLOCK_EXECUTOR_NAME, eventeumBlock.getNodeName()), () -> {
            final BigInteger expectedBlock = BigInteger.valueOf(lastBlockNumberProcessed.get()).add(BigInteger.ONE);

            //A lower or equal block is valid due to forking or replaying on failure
            if (lastBlockNumberProcessed.get() > 0 && eventeumBlock.getNumber().compareTo(expectedBlock) > 0) {

                final int missingBlocks = eventeumBlock.getNumber()
                        .subtract(expectedBlock)
                        .intValue();

                log.warn("Missing {} blocks.  Expected {}, got {}.  Catching up...",
                        missingBlocks, expectedBlock, eventeumBlock.getNumber());

                //Get each missing block and process before continuing with block that was passed into method
                for(int i = 0; i < missingBlocks; i++) {
                    final BigInteger nextBlock = expectedBlock.add(BigInteger.valueOf(i));

                    try {
                        log.warn("Retrieving block number {}...", nextBlock);
                        final Block block = getBlockWithNumber(nextBlock);

                        blockListeners.forEach(listener -> triggerListener(listener, block));
                        updateLastBlockProcessed(block);
                    } catch (Throwable t) {
                        onError(blockSubscription, t);
                    }
                }
            }

            blockListeners.forEach(listener -> triggerListener(listener, eventeumBlock));
            updateLastBlockProcessed(eventeumBlock);
        });
    }

    protected void triggerListener(BlockListener listener, Block block) {
        if (!errored.get()) {
            try {
                listener.onBlock(block);
            } catch (Throwable t) {
                onError(blockSubscription, t);
            }
        }
    }

    protected BigInteger getStartBlock() {
        return blockNumberService.getStartBlockForNode(nodeName);
    }

    protected void onError(Disposable disposable, Throwable error) {
        log.error("There was an error when processing a block, disposing blocksubscription (will be reinitialised)", error);

        errored.set(true);
        disposable.dispose();
    }

    private void updateLastBlockProcessed(Block block) {
        lastBlockNumberProcessed.set(block.getNumber().longValue());
    }

    private Block getBlockWithNumber(BigInteger blockNumber) throws IOException {
        final EthBlock ethBlock = web3j.ethGetBlockByNumber(
                DefaultBlockParameter.valueOf(blockNumber), true).send();

        return new Web3jBlock(ethBlock.getBlock(), nodeName);
    }

    abstract Block convertToEventeumBlock(T blockObject);

}
