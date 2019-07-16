package net.consensys.eventeum.chain.service;

import com.google.common.collect.EvictingQueue;
import net.consensys.eventeum.dto.block.BlockDetails;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class DefaultBlockCache implements BlockCache {

    private static final Integer CACHE_SIZE = 3;

    private EvictingQueue<BlockDetails> cachedBlocks = EvictingQueue.create(CACHE_SIZE);

    private ReentrantLock lock = new ReentrantLock();

    @Override
    public void add(BlockDetails blockDetails) {
        lock.lock();

        try {
            cachedBlocks.add(blockDetails);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<BlockDetails> getCachedBlocks() {
        lock.lock();

        try {
            final Set<BlockDetails> cachedBlocksSet = new HashSet<>();
            cachedBlocksSet.addAll(cachedBlocks);

            return cachedBlocksSet;
        } finally {
            lock.unlock();
        }
    }
}
