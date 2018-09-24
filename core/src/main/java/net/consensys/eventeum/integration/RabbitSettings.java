package net.consensys.eventeum.integration;

import lombok.Data;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * An encapsulation of Rabbit related properties.
 *
 * @author ioBuilders <tech@io.builders>
 */
@Configuration
@ConfigurationProperties(prefix = "rabbitmq")
@Data
public class RabbitSettings {

    private String exchange;

    private String routingKeyPrefix;

    @Bean
    Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }


}
