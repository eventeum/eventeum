package net.consensys.eventeum.chain.service;

import com.google.common.collect.EvictingQueue;
import net.consensys.eventeum.chain.service.domain.Block;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class DefaultBlockCache implements BlockCache {

    private static final Integer CACHE_SIZE = 3;

    private EvictingQueue<Block> cachedBlocks = EvictingQueue.create(CACHE_SIZE);

    private ReentrantLock lock = new ReentrantLock();

    @Override
    public void add(Block block) {
        lock.lock();

        try {
            cachedBlocks.add(block);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<Block> getCachedBlocks() {
        lock.lock();

        try {
            final Set<Block> cachedBlocksSet = new HashSet<>();
            cachedBlocksSet.addAll(cachedBlocks);

            return cachedBlocksSet;
        } finally {
            lock.unlock();
        }
    }
}
