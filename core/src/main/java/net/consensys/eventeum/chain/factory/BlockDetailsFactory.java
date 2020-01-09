package net.consensys.eventeum.chain.factory;

import net.consensys.eventeum.BlockDetails;
import net.consensys.eventeum.chain.service.domain.Block;

public interface BlockDetailsFactory {

    BlockDetails createBlockDetails(Block block);
}
