package io.keyko.monitoring.agent.core.chain.service.strategy;

import io.keyko.monitoring.agent.core.model.LatestBlock;
import io.keyko.monitoring.agent.core.utils.ExecutorNameFactory;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import io.keyko.monitoring.agent.core.chain.block.BlockListener;
import io.keyko.monitoring.agent.core.chain.service.domain.Block;
import io.keyko.monitoring.agent.core.service.AsyncTaskService;
import io.keyko.monitoring.agent.core.service.EventStoreService;
import org.web3j.protocol.Web3j;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public abstract class AbstractBlockSubscriptionStrategy<T> implements BlockSubscriptionStrategy {

    protected static final String BLOCK_EXECUTOR_NAME = "BLOCK";

    protected Collection<BlockListener> blockListeners = new ConcurrentLinkedQueue<>();
    protected Disposable blockSubscription;
    protected Web3j web3j;
    protected EventStoreService eventStoreService;
    protected String nodeName;
    protected AsyncTaskService asyncService;

    public AbstractBlockSubscriptionStrategy(Web3j web3j,
                                             String nodeName,
                                             EventStoreService eventStoreService,
                                             AsyncTaskService asyncService) {
        this.web3j = web3j;
        this.nodeName = nodeName;
        this.eventStoreService = eventStoreService;
        this.asyncService = asyncService;
    }

    @Override
    public void unsubscribe() {
        try {
            if (blockSubscription != null) {
                blockSubscription.dispose();
            }
        } finally {
            blockSubscription = null;
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
        triggerListeners(eventeumBlock);
    }

    protected void triggerListeners(Block eventeumBlock) {
        asyncService.execute(ExecutorNameFactory.build(BLOCK_EXECUTOR_NAME, eventeumBlock.getNodeName()), () -> {
            blockListeners.forEach(listener -> triggerListener(listener, eventeumBlock));
        });
    }

    protected void triggerListener(BlockListener listener, Block block) {
        try {
            listener.onBlock(block);
        } catch(Throwable t) {
            log.error(String.format("An error occured when processing block with hash %s", block.getHash()), t);
        }
    }

    protected Optional<LatestBlock> getLatestBlock() {
        return eventStoreService.getLatestBlock(nodeName);
    }

    abstract Block convertToEventeumBlock(T blockObject);

}
