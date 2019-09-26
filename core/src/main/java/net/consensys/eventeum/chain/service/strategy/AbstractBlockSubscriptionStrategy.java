package net.consensys.eventeum.chain.service.strategy;

import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.integration.eventstore.EventStore;
import net.consensys.eventeum.model.LatestBlock;
import net.consensys.eventeum.service.EventStoreService;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import rx.Subscription;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public abstract class AbstractBlockSubscriptionStrategy<T> implements BlockSubscriptionStrategy {

    private Lock lock = new ReentrantLock();

    protected Collection<BlockListener> blockListeners = new ConcurrentLinkedQueue<>();
    protected Disposable blockSubscription;
    protected Web3j web3j;
    protected EventStoreService eventStoreService;
    protected String nodeName;

    public AbstractBlockSubscriptionStrategy(Web3j web3j, String nodeName, EventStoreService eventStoreService) {
        this.web3j = web3j;
        this.nodeName = nodeName;
        this.eventStoreService = eventStoreService;
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
        lock.lock();
        try {
            blockListeners.forEach(listener -> triggerListener(listener, convertToEventeumBlock(blockObject)));
        } finally {
            lock.unlock();
        }
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
