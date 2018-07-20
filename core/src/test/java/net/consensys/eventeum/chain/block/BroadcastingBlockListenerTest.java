package net.consensys.eventeum.chain.block;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BroadcastingBlockListenerTest {

    private BroadcastingBlockListener underTest;

    private BlockchainEventBroadcaster mockBroadcaster;

    @Before
    public void init() {
        mockBroadcaster = mock(BlockchainEventBroadcaster.class);

        underTest = new BroadcastingBlockListener(mockBroadcaster);
    }

    @Test
    public void testOnBlock() {
        final BlockDetails blockDetails = new BlockDetails();
        underTest.onBlock(blockDetails);

        verify(mockBroadcaster).broadcastNewBlock(blockDetails);
    }
}
