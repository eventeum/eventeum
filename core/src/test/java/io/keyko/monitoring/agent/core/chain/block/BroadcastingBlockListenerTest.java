package io.keyko.monitoring.agent.core.chain.block;

import io.keyko.monitoring.agent.core.chain.factory.DefaultBlockDetailsFactory;
import io.keyko.monitoring.agent.core.chain.service.domain.Block;
import io.keyko.monitoring.agent.core.dto.block.BlockDetails;
import io.keyko.monitoring.agent.core.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class BroadcastingBlockListenerTest {

    private BroadcastingBlockListener underTest;

    private BlockchainEventBroadcaster mockBroadcaster;

    @Before
    public void init() {
        mockBroadcaster = mock(BlockchainEventBroadcaster.class);

        underTest = new BroadcastingBlockListener(mockBroadcaster, new DefaultBlockDetailsFactory());
    }

    @Test
    public void testOnBlock() {
        final Block block = Mockito.mock(Block.class);
        when(block.getNumber()).thenReturn(BigInteger.TEN);
        underTest.onBlock(block);

        ArgumentCaptor<BlockDetails> captor = ArgumentCaptor.forClass(BlockDetails.class);
        verify(mockBroadcaster).broadcastNewBlock(captor.capture());

        assertEquals(BigInteger.TEN, captor.getValue().getNumber());
    }
}
