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

package net.consensys.eventeum.chain.contract;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * A contract event listener that broadcasts the event details via the configured broadcaster.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BroadcastingEventListener implements ContractEventListener {

    private BlockchainEventBroadcaster eventBroadcaster;

    @Autowired
    public BroadcastingEventListener(BlockchainEventBroadcaster eventBroadcaster) {
        this.eventBroadcaster = eventBroadcaster;
    }

    @Override
    public void onEvent(ContractEventDetails eventDetails) {
        eventBroadcaster.broadcastContractEvent(eventDetails);
    }
}
