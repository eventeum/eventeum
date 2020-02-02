package io.keyko.monitoring.agent.core.chain.factory;

import io.keyko.monitoring.agent.core.chain.service.domain.Block;
import io.keyko.monitoring.agent.core.dto.block.BlockDetails;

public interface BlockDetailsFactory {

    BlockDetails createBlockDetails(Block block);
}
