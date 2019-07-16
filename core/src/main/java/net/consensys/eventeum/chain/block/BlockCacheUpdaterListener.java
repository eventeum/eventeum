package net.consensys.eventeum.chain.block;

import lombok.AllArgsConstructor;
import net.consensys.eventeum.chain.service.BlockCache;
import net.consensys.eventeum.dto.block.BlockDetails;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class BlockCacheUpdaterListener implements BlockListener {

    private BlockCache blockCache;

    @Override
    public void onBlock(BlockDetails blockDetails) {
        blockCache.add(blockDetails);
    }
}
