package net.consensys.eventeum.integration;

import lombok.Data;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * An encapsulation of Rabbit related properties.
 *
 * @author ioBuilders <tech@io.builders>
 */
@Configuration
@Data
public class RabbitSettings {
    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.blockNotification}")
    private boolean blockNotification;

    @Value("${rabbitmq.routingKey.contractEvents}")
    private String contractEventsRoutingKey;

    @Value("${rabbitmq.routingKey.blockEvents}")
    private String blockEventsRoutingKey;

    @Value("${rabbitmq.routingKey.transactionEvents}")
    private String transactionEventsRoutingKey;

    @Bean
    Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }


}
