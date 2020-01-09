package net.consensys.eventeum.chain.block;

import net.consensys.eventeum.chain.factory.BlockDetailsFactory;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
import net.consensys.eventeum.model.LatestBlock;
import net.consensys.eventeum.monitoring.EventeumValueMonitor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A contract event listener that saves the ContractEventDetails to a SaveableEventStore.
 * <p>
 * Only gets registered if a SaveableEventStore exists in the context.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
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

        chainServicesContainer.getNodeNames().forEach(node -> {
            this.latestBlockMap.put(node, valueMonitor.monitor("latestBlock", node, new AtomicLong(0)));
        });
    }

    @Override
    public void onBlock(Block block) {
        saveableEventStore.save(new LatestBlock(blockDetailsFactory.createBlockDetails(block)));
        latestBlockMap.get(block.getNodeName()).set(block.getNumber().longValue());

    }
}
