package net.consensys.eventeum.config;

import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(name = "broadcaster.type", havingValue = "RABBIT")
@Import(RabbitAutoConfiguration.class)
class RabbitConfiguration {}
