package net.consensys.eventeum.config;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.integration.KafkaSettings;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.integration.broadcast.blockchain.EventBroadcasterWrapper;
import net.consensys.eventeum.integration.broadcast.blockchain.KafkaBlockchainEventBroadcaster;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Spring bean configuration for the BlockchainEventBroadcaster.
 * <p>
 * Registers a broadcaster bean based on the value of the broadcaster.type property.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Configuration
public class BlockchainEventBroadcasterConfiguration {

    private static final String EXPIRATION_PROPERTY = "${broadcaster.cache.expirationMillis}";
    private static final String BROADCASTER_PROPERTY = "broadcaster.type";
    private static final String ENABLE_BLOCK_NOTIFICATIONS = "${broadcaster.enableBlockNotifications:true}";

    private Long onlyOnceCacheExpirationTime;
    private boolean enableBlockNotifications;

    @Autowired
    public BlockchainEventBroadcasterConfiguration(@Value(EXPIRATION_PROPERTY) Long onlyOnceCacheExpirationTime, @Value(ENABLE_BLOCK_NOTIFICATIONS) boolean enableBlockNotifications) {
        this.onlyOnceCacheExpirationTime = onlyOnceCacheExpirationTime;
        this.enableBlockNotifications = enableBlockNotifications;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = BROADCASTER_PROPERTY, havingValue = "KAFKA")
    public BlockchainEventBroadcaster kafkaBlockchainEventBroadcaster(KafkaTemplate<String, GenericRecord> kafkaTemplate,
                                                                      KafkaSettings kafkaSettings,
                                                                      CrudRepository<ContractEventFilter, String> filterRepository) {
        final BlockchainEventBroadcaster broadcaster =
                new KafkaBlockchainEventBroadcaster(kafkaTemplate, kafkaSettings, filterRepository);

        return onlyOnceWrap(broadcaster);
    }


    private BlockchainEventBroadcaster onlyOnceWrap(BlockchainEventBroadcaster toWrap) {
        return new EventBroadcasterWrapper(onlyOnceCacheExpirationTime, toWrap, enableBlockNotifications);
    }
}
