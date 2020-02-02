package io.keyko.monitoring.agent.core.chain.factory;

import io.keyko.monitoring.agent.core.chain.service.domain.Block;
import io.keyko.monitoring.agent.core.dto.block.BlockDetails;
import org.springframework.stereotype.Component;

@Component
public class DefaultBlockDetailsFactory implements BlockDetailsFactory {

    @Override
    public BlockDetails createBlockDetails(Block block) {
        final BlockDetails blockDetails = new BlockDetails();

        blockDetails.setNumber(block.getNumber());
        blockDetails.setHash(block.getHash());
        blockDetails.setTimestamp(block.getTimestamp());
        blockDetails.setNodeName(block.getNodeName());

        return blockDetails;
    }
}
