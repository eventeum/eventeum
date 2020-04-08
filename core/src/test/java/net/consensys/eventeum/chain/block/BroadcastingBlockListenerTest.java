/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.consensys.eventeum.chain.block;

import net.consensys.eventeum.chain.factory.DefaultBlockDetailsFactory;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.wrapper.Web3jBlock;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.web3j.utils.Numeric;

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
