package io.keyko.monitoring.agent.core.dto.message;

import io.keyko.monitoring.agent.core.dto.block.BlockDetails;


public class BlockEvent extends AbstractMessage<BlockDetails> {
    public static final String TYPE = "BLOCK";

    public BlockEvent() {
    }

    public BlockEvent(BlockDetails details) {
        super(details.getHash(), TYPE, details);
    }
}