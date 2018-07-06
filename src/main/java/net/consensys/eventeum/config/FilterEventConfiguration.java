package net.consensys.eventeum.config;

import net.consensys.eventeum.dto.message.Message;
import net.consensys.eventeum.integration.KafkaSettings;
import net.consensys.eventeum.integration.broadcast.filter.DoNothingFilterEventBroadcaster;
import net.consensys.eventeum.integration.broadcast.filter.FilterEventBroadcaster;
import net.consensys.eventeum.integration.broadcast.filter.KafkaFilterEventBroadcaster;
import net.consensys.eventeum.integration.consumer.FilterEventConsumer;
import net.consensys.eventeum.integration.consumer.KafkaFilterEventConsumer;
import net.consensys.eventeum.service.SubscriptionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Spring bean configuration for the FilterEvent broadcaster and consumer.
 *
 * If broadcaster.multiInstance is set to true, then register a Kafka broadcaster,
 * otherwise register a dummy broadcaster that does nothing.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Configuration
public class FilterEventConfiguration {

    @Bean
    @ConditionalOnProperty(name="broadcaster.multiInstance", havingValue="true")
    public FilterEventBroadcaster kafkaFilterEventBroadcaster(KafkaTemplate<String, Message> kafkaTemplate,
                                                               KafkaSettings kafkaSettings) {
        return new KafkaFilterEventBroadcaster(kafkaTemplate, kafkaSettings);
    }

    @Bean
    @ConditionalOnProperty(name="broadcaster.multiInstance", havingValue="true")
    public FilterEventConsumer kafkaFilterEventConsumer(SubscriptionService subscriptionService,
                                                        KafkaSettings kafkaSettings) {
        return new KafkaFilterEventConsumer(subscriptionService, kafkaSettings);
    }

    @Bean
    @ConditionalOnProperty(name="broadcaster.multiInstance", havingValue="false")
    public FilterEventBroadcaster doNothingFilterEventBroadcaster() {
        return new DoNothingFilterEventBroadcaster();
    }
}
