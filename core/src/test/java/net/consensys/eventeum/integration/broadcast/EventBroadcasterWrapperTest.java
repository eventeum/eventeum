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

package net.consensys.eventeum.integration.broadcast;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.integration.broadcast.blockchain.EventBroadcasterWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class EventBroadcasterWrapperTest {

    private static final Long EXPIRATION_MILLISECONDS = 6000000L;

    private BlockchainEventBroadcaster blockchainEventBroadcaster;

    @BeforeEach
    public void init() {
        blockchainEventBroadcaster = Mockito.mock(BlockchainEventBroadcaster.class);
    }

    @Test
    public void testDisableBlockNotification() {
        EventBroadcasterWrapper underTest = new EventBroadcasterWrapper(EXPIRATION_MILLISECONDS,
                blockchainEventBroadcaster, false);
        final BlockDetails block = new BlockDetails();

        underTest.broadcastNewBlock(block);

        verify(blockchainEventBroadcaster, never()).broadcastNewBlock(block);
    }

    @Test
    public void testEnableBlockNotifications() {
        EventBroadcasterWrapper underTest = new EventBroadcasterWrapper(EXPIRATION_MILLISECONDS,
                blockchainEventBroadcaster, true);
        final BlockDetails block = new BlockDetails();

        underTest.broadcastNewBlock(block);

        verify(blockchainEventBroadcaster).broadcastNewBlock(block);
    }
}
