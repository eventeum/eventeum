package net.consensys.eventeum.chain.factory;

import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.dto.block.BlockDetails;

public interface BlockDetailsFactory {

    BlockDetails createBlockDetails(Block block);
}
