package net.consensys.eventeum.chain.service;

import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.dto.block.BlockDetails;

import java.util.Set;

public interface BlockCache {

    void add(Block block);

    Set<Block> getCachedBlocks();
}
