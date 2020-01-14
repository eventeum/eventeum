package net.consensys.eventeum.chain.factory;

import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.dto.block.BlockDetails;
import org.springframework.stereotype.Component;
import org.web3j.utils.Numeric;

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
