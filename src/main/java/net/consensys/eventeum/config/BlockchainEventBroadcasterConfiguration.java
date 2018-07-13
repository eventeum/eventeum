package net.consensys.eventeum.config;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.Message;
import net.consensys.eventeum.integration.KafkaSettings;
import net.consensys.eventeum.integration.broadcast.blockchain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * Spring bean configuration for the BlockchainEventBroadcaster.
 *
 * Registers a broadcaster bean based on the value of the broadcaster.type property.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Configuration
public class BlockchainEventBroadcasterConfiguration {

    private static final String EXPIRATION_PROPERTY = "${broadcaster.cache.expirationMillis}";

    private Long onlyOnceCacheExpirationTime;

    @Autowired
    public BlockchainEventBroadcasterConfiguration(@Value(EXPIRATION_PROPERTY) Long onlyOnceCacheExpirationTime) {
        this.onlyOnceCacheExpirationTime = onlyOnceCacheExpirationTime;
    }

    @Bean
    @ConditionalOnProperty(name="broadcaster.type", havingValue="KAFKA")
    public BlockchainEventBroadcaster kafkaBlockchainEventBroadcaster(KafkaTemplate<String, Message> kafkaTemplate,
                                                                      KafkaSettings kafkaSettings,
                                                                      CrudRepository<ContractEventFilter, String> filterRepository) {
        final BlockchainEventBroadcaster broadcaster =
                new KafkaBlockchainEventBroadcaster(kafkaTemplate, kafkaSettings, filterRepository);

        return onlyOnceWrap(broadcaster);
    }

    @Bean
    @ConditionalOnProperty(name="broadcaster.type", havingValue="HTTP")
    public BlockchainEventBroadcaster httpBlockchainEventBroadcaster(HttpBroadcasterSettings settings) {
        final BlockchainEventBroadcaster broadcaster =
                new HttpBlockchainEventBroadcaster(settings, retryTemplate());

        return onlyOnceWrap(broadcaster);
    }

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(3000l);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

    private BlockchainEventBroadcaster onlyOnceWrap(BlockchainEventBroadcaster toWrap) {
        return new OnlyOnceBlockchainEventBroadcasterWrapper(onlyOnceCacheExpirationTime, toWrap);
    }
}
