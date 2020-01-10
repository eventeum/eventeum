package net.consensys.eventeum.chain.factory;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.chain.service.domain.Block;

public interface BlockDetailsFactory {

    BlockDetails createBlockDetails(Block block);
}
