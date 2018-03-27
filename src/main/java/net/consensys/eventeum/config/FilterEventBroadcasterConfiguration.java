package net.consensys.eventeum.config;

import net.consensys.eventeum.dto.message.Message;
import net.consensys.eventeum.integration.KafkaSettings;
import net.consensys.eventeum.integration.broadcast.DoNothingFilterEventBroadcaster;
import net.consensys.eventeum.integration.broadcast.FilterEventBroadcaster;
import net.consensys.eventeum.integration.broadcast.KafkaFilterEventBroadcaster;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Spring bean configuration for the FilterEventBroadcaster.
 *
 * If broadcaster.multiInstance is set to true, then register a Kafka broadcaster,
 * otherwise register a dummy broadcaster that does nothing.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Configuration
public class FilterEventBroadcasterConfiguration {

    @Bean
    @ConditionalOnProperty(name="broadcaster.multiInstance", havingValue="true")
    public FilterEventBroadcaster kafkaFilterEventBroadcaster(KafkaTemplate<String, Message> kafkaTemplate,
                                                               KafkaSettings kafkaSettings) {
        return new KafkaFilterEventBroadcaster(kafkaTemplate, kafkaSettings);
    }

    @Bean
    @ConditionalOnProperty(name="broadcaster.multiInstance", havingValue="false")
    public FilterEventBroadcaster doNothingFilterEventBroadcaster() {
        return new DoNothingFilterEventBroadcaster();
    }
}
