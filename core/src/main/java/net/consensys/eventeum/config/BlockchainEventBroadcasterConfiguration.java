package net.consensys.eventeum.config;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.EventeumMessage;
import net.consensys.eventeum.integration.KafkaSettings;
import net.consensys.eventeum.integration.RabbitSettings;
import net.consensys.eventeum.integration.broadcast.blockchain.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private static final String BROADCASTER_PROPERTY = "broadcaster.type";

    private Long onlyOnceCacheExpirationTime;

    @Autowired
    public BlockchainEventBroadcasterConfiguration(@Value(EXPIRATION_PROPERTY) Long onlyOnceCacheExpirationTime) {
        this.onlyOnceCacheExpirationTime = onlyOnceCacheExpirationTime;
    }

    @Bean
    @ConditionalOnProperty(name= BROADCASTER_PROPERTY, havingValue="KAFKA")
    public BlockchainEventBroadcaster kafkaBlockchainEventBroadcaster(KafkaTemplate<String, EventeumMessage> kafkaTemplate,
                                                                      KafkaSettings kafkaSettings,
                                                                      CrudRepository<ContractEventFilter, String> filterRepository) {
        final BlockchainEventBroadcaster broadcaster =
                new KafkaBlockchainEventBroadcaster(kafkaTemplate, kafkaSettings, filterRepository);

        return onlyOnceWrap(broadcaster);
    }

    @Bean
    @ConditionalOnProperty(name=BROADCASTER_PROPERTY, havingValue="HTTP")
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

    @Bean
    @ConditionalOnProperty(name=BROADCASTER_PROPERTY, havingValue="RABBIT")
    public BlockchainEventBroadcaster rabbitBlockChainEventBroadcaster(RabbitTemplate rabbitTemplate, RabbitSettings rabbitSettings) {
        final BlockchainEventBroadcaster broadcaster =
                new RabbitBlockChainEventBroadcaster(rabbitTemplate,rabbitSettings);

        return onlyOnceWrap(broadcaster);
    }

    private BlockchainEventBroadcaster onlyOnceWrap(BlockchainEventBroadcaster toWrap) {
        return new OnlyOnceBlockchainEventBroadcasterWrapper(onlyOnceCacheExpirationTime, toWrap);
    }
}
