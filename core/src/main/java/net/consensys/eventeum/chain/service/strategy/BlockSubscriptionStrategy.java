package net.consensys.eventeum.chain.service.strategy;

import io.reactivex.disposables.Disposable;
import net.consensys.eventeum.chain.block.BlockListener;

public interface BlockSubscriptionStrategy {
    Disposable subscribe();

    void unsubscribe();

    void addBlockListener(BlockListener blockListener);

    void removeBlockListener(BlockListener blockListener);

    boolean isSubscribed();
}
