package net.consensys.eventeum.integration.broadcast;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.Message;
import net.consensys.eventeum.integration.KafkaSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * {@inheritDoc}
 *
 * This broadcaster also ensures that the same message is only sent once
 * (by storing sent events in a short lives cache and not sending events if a cache match is found).
 *
 * The cache expiration time can be configured with the broadcaster.cache.expirationMillis property.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
@Profile("default")
public class OnlyOnceBlockchainEventBroadcaster extends KafkaBlockchainEventBroadcaster {

    private static final String EXPIRATION_PROPERTY = "${broadcaster.cache.expirationMillis}";

    private Cache<Integer, ContractEventDetails> contractEventCache;

    private Long expirationTimeMillis;

    @Autowired
    OnlyOnceBlockchainEventBroadcaster(KafkaTemplate<String, Message> kafkaTemplate,
                                       KafkaSettings kafkaSettings,
                                       @Value(EXPIRATION_PROPERTY) Long expirationTimeMillis,
                                       CrudRepository<ContractEventFilter, String> filterRepository) {
        super(kafkaTemplate, kafkaSettings, filterRepository);
        this.expirationTimeMillis = expirationTimeMillis;
        this.contractEventCache = createContractEventCache();
    }

    @Override
    public void broadcastNewBlock(BlockDetails block) {
        super.broadcastNewBlock(block);
    }

    @Override
    public void broadcastContractEvent(ContractEventDetails eventDetails) {
        if (contractEventCache.getIfPresent(Integer.valueOf(eventDetails.hashCode())) == null) {
            contractEventCache.put(Integer.valueOf(eventDetails.hashCode()), eventDetails);
            super.broadcastContractEvent(eventDetails);
        }
    }

    @Scheduled(fixedRateString = EXPIRATION_PROPERTY)
    public void cleanUpCache() {
        contractEventCache.cleanUp();
    }

    protected Cache<Integer, ContractEventDetails> createContractEventCache() {
        return CacheBuilder.newBuilder()
            .expireAfterWrite(expirationTimeMillis, TimeUnit.MILLISECONDS)
            .build();
    }
}
