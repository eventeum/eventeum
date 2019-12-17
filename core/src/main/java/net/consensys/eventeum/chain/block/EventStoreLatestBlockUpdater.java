
package net.consensys.eventeum.chain.block;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import net.consensys.eventeum.chain.factory.BlockDetailsFactory;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
import net.consensys.eventeum.model.LatestBlock;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A contract event listener that saves the ContractEventDetails to a SaveableEventStore.
 *
 * Only gets registered if a SaveableEventStore exists in the context.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class EventStoreLatestBlockUpdater implements BlockListener {

    private SaveableEventStore saveableEventStore;
    private MeterRegistry meterRegisrty;

    private BlockDetailsFactory blockDetailsFactory;
    private Map<String ,AtomicLong> latestBlockMap;

    @Autowired
    public EventStoreLatestBlockUpdater(SaveableEventStore saveableEventStore,BlockDetailsFactory blockDetailsFactory, MeterRegistry meterRegistry, ChainServicesContainer chainServicesContainer) {
        this.saveableEventStore = saveableEventStore;
        this.meterRegisrty = meterRegistry;
        this.latestBlockMap = new HashMap<>();
        this.blockDetailsFactory = blockDetailsFactory;

        chainServicesContainer.getNodeNames().forEach( node -> {
            this.latestBlockMap.put(node, meterRegistry.gauge("eventeum."+ node +".latestblock", Tags.of("chain",node),new AtomicLong(0)));
        });



    }

    @Override
    public void onBlock(Block block) {
        saveableEventStore.save(new LatestBlock(blockDetailsFactory.createBlockDetails(block)));
        latestBlockMap.get(block.getNodeName()).set(block.getNumber().longValue());

    }
}
