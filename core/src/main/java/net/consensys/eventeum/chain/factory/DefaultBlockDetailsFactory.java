package net.consensys.eventeum.chain.factory;

import net.consensys.eventeum.BlockDetails;
import net.consensys.eventeum.chain.service.domain.Block;
import org.springframework.stereotype.Component;

@Component
public class DefaultBlockDetailsFactory implements BlockDetailsFactory {

    @Override
    public BlockDetails createBlockDetails(Block block) {
        final BlockDetails blockDetails = new BlockDetails();

        blockDetails.setNumber(block.getNumber().toString());
        blockDetails.setHash(block.getHash());
        blockDetails.setTimestamp(block.getTimestamp().toString());
        blockDetails.setNodeName(block.getNodeName());

        return blockDetails;
    }
}
