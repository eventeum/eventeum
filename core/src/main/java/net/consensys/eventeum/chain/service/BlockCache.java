package net.consensys.eventeum.chain.service;

import net.consensys.eventeum.dto.block.BlockDetails;

import java.util.Set;

public interface BlockCache {

    void add(BlockDetails blockDetails);

    Set<BlockDetails> getCachedBlocks();
}
