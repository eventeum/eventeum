package io.keyko.monitoring.agent.core.chain.service;

import io.keyko.monitoring.agent.core.chain.service.domain.Block;

import java.util.Set;

public interface BlockCache {

    void add(Block block);

    Set<Block> getCachedBlocks();
}
