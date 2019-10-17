package net.consensys.eventeum.integration.broadcast;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.integration.broadcast.blockchain.EventBroadcasterWrapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class EventBroadcasterWrapperTest {

    private static final Long EXPIRATION_MILLISECONDS = 6000000L;

    private BlockchainEventBroadcaster blockchainEventBroadcaster;

    @Before
    public void init() {
        blockchainEventBroadcaster = Mockito.mock(BlockchainEventBroadcaster.class);
    }

    @Test
    public void testDisableBlockNotification() {
        EventBroadcasterWrapper underTest = new EventBroadcasterWrapper(EXPIRATION_MILLISECONDS, blockchainEventBroadcaster, false);
        final BlockDetails block = new BlockDetails();

        underTest.broadcastNewBlock(block);

        verify(blockchainEventBroadcaster, never()).broadcastNewBlock(block);
    }

    @Test
    public void testEnableBlockNotifications() {
        EventBroadcasterWrapper underTest = new EventBroadcasterWrapper(EXPIRATION_MILLISECONDS, blockchainEventBroadcaster, true);
        final BlockDetails block = new BlockDetails();

        underTest.broadcastNewBlock(block);

        verify(blockchainEventBroadcaster).broadcastNewBlock(block);
    }
}
