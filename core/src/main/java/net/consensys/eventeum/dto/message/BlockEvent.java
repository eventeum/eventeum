package net.consensys.eventeum.dto.message;

import net.consensys.eventeum.BlockDetails;


public class BlockEvent extends AbstractMessage<BlockDetails> {
    public static final String TYPE = "BLOCK";

    public BlockEvent() {
    }

    public BlockEvent(BlockDetails details) {
        super(details.getHash(), TYPE, details);
    }
}