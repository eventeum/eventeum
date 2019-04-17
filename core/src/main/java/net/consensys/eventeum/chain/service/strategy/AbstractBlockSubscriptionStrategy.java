package net.consensys.eventeum.chain.service.strategy;

import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.dto.block.BlockDetails;
import org.web3j.protocol.Web3j;
import rx.Subscription;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public abstract class AbstractBlockSubscriptionStrategy<T> implements BlockSubscriptionStrategy {

    private Lock lock = new ReentrantLock();

    protected Collection<BlockListener> blockListeners = new ConcurrentLinkedQueue<>();
    protected Subscription blockSubscription;
    protected Web3j web3j;
    protected String nodeName;

    public AbstractBlockSubscriptionStrategy(Web3j web3j, String nodeName) {
        this.web3j = web3j;
        this.nodeName = nodeName;
    }

    @Override
    public void unsubscribe() {
        try {
            if (blockSubscription != null) {
                blockSubscription.unsubscribe();
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
        return blockSubscription != null && !blockSubscription.isUnsubscribed();
    }

    protected void triggerListeners(T blockObject) {
        lock.lock();
        try {
            blockListeners.forEach(listener -> triggerListener(listener, convertToBlockDetails(blockObject)));
        } finally {
            lock.unlock();
        }
    }

    protected void triggerListener(BlockListener listener, BlockDetails block) {
        try {
            listener.onBlock(block);
        } catch(Throwable t) {
            log.error(String.format("An error occured when processing block with hash %s", block.getHash()), t);
        }
    }

    abstract BlockDetails convertToBlockDetails(T blockObject);

}
