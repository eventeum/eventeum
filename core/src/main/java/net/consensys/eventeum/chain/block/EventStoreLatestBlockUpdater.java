
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

import net.consensys.eventeum.chain.factory.BlockDetailsFactory;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
import net.consensys.eventeum.model.LatestBlock;
import net.consensys.eventeum.monitoring.EventeumValueMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A block listener that saves the ContractEventDetails to a SaveableEventStore.
 *
 * Only gets registered if a SaveableEventStore exists in the context.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class EventStoreLatestBlockUpdater implements BlockListener {

    private SaveableEventStore saveableEventStore;

    private BlockDetailsFactory blockDetailsFactory;
    private Map<String, AtomicLong> latestBlockMap;

    @Autowired
    public EventStoreLatestBlockUpdater(SaveableEventStore saveableEventStore,
                                        BlockDetailsFactory blockDetailsFactory,
                                        EventeumValueMonitor valueMonitor,
                                        ChainServicesContainer chainServicesContainer) {
        this.saveableEventStore = saveableEventStore;
        this.latestBlockMap = new HashMap<>();
        this.blockDetailsFactory = blockDetailsFactory;

        chainServicesContainer.getNodeNames().forEach( node -> {
            this.latestBlockMap.put(node, valueMonitor.monitor("latestBlock", node, new AtomicLong(0)));
        });
    }

    @Override
    public void onBlock(Block block) {
        saveableEventStore.save(new LatestBlock(blockDetailsFactory.createBlockDetails(block)));
        latestBlockMap.get(block.getNodeName()).set(block.getNumber().longValue());

    }
}
