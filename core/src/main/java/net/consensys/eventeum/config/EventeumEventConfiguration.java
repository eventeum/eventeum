package net.consensys.eventeum.config;

import net.consensys.eventeum.dto.message.EventeumMessage;
import net.consensys.eventeum.integration.KafkaSettings;
import net.consensys.eventeum.integration.broadcast.internal.DoNothingEventeumEventBroadcaster;
import net.consensys.eventeum.integration.broadcast.internal.EventeumEventBroadcaster;
import net.consensys.eventeum.integration.broadcast.internal.HazelCastEventeumEventBroadcaster;
import net.consensys.eventeum.integration.broadcast.internal.KafkaEventeumEventBroadcaster;
import net.consensys.eventeum.integration.consumer.EventeumInternalEventConsumer;
import net.consensys.eventeum.integration.consumer.HazelCastEventConsumer;
import net.consensys.eventeum.integration.consumer.KafkaFilterEventConsumer;
import net.consensys.eventeum.service.SubscriptionService;
import net.consensys.eventeum.service.TransactionMonitoringService;

import com.hazelcast.core.HazelcastInstance;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
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
public class EventeumEventConfiguration {

    @Bean
    @ConditionalOnExpression("'${broadcaster.multiInstance}:${broadcaster.multiInstanceType}'=='true:Kafka'")
    public EventeumEventBroadcaster kafkaFilterEventBroadcaster(KafkaTemplate<String, EventeumMessage> kafkaTemplate,
                                                                KafkaSettings kafkaSettings) {
        return new KafkaEventeumEventBroadcaster(kafkaTemplate, kafkaSettings);
    }

    @Bean
    @ConditionalOnExpression("'${broadcaster.multiInstance}:${broadcaster.multiInstanceType}'=='true:Kafka'")
    public EventeumInternalEventConsumer kafkaFilterEventConsumer(SubscriptionService subscriptionService,
                                                                  TransactionMonitoringService transactionMonitoringService,
                                                                  KafkaSettings kafkaSettings) {
        return new KafkaFilterEventConsumer(subscriptionService, transactionMonitoringService, kafkaSettings);
    }

    @Bean
    @ConditionalOnExpression("'${broadcaster.multiInstance}:${broadcaster.multiInstanceType}'=='true:HazelCast'")
    public EventeumEventBroadcaster hazelCastEventeumEventBroadcaster(HazelcastInstance hazelcastInstance) {
        return new HazelCastEventeumEventBroadcaster(hazelcastInstance);
    }

    @Bean
    @ConditionalOnExpression("'${broadcaster.multiInstance}:${broadcaster.multiInstanceType}'=='true:HazelCast'")
    public EventeumInternalEventConsumer hazelcastEventeumEventConsumer(SubscriptionService subscriptionService,
                                                                  TransactionMonitoringService transactionMonitoringService,
								  HazelcastInstance hazelcastInstance) {
        return new HazelCastEventConsumer(subscriptionService, transactionMonitoringService, hazelcastInstance);
    }

    @Bean
    @ConditionalOnProperty(name="broadcaster.multiInstance", havingValue="false")
    public EventeumEventBroadcaster doNothingFilterEventBroadcaster() {
        return new DoNothingEventeumEventBroadcaster();
    }
}
