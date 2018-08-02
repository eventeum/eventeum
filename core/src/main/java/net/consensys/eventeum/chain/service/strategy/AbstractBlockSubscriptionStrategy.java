package net.consensys.eventeum.chain.service.strategy;

import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.service.AsyncTaskService;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import rx.Subscription;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractBlockSubscriptionStrategy implements BlockSubscriptionStrategy {
    protected Collection<BlockListener> blockListeners = new ConcurrentLinkedQueue<>();
    protected Subscription blockSubscription;
    protected Web3j web3j;
    protected AsyncTaskService asyncTaskService;

    public AbstractBlockSubscriptionStrategy(Web3j web3j, AsyncTaskService asyncTaskService) {
        this.web3j = web3j;
        this.asyncTaskService = asyncTaskService;
    }

    @Override
    public void unsubscribe() {
        blockSubscription.unsubscribe();
    }

    @Override
    public void addBlockListener(BlockListener blockListener) {
        blockListeners.add(blockListener);
    }

    @Override
    public void removeBlockListener(BlockListener blockListener) {
        blockListeners.remove(blockListener);
    }

}
