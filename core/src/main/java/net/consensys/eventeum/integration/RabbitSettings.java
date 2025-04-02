/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.consensys.eventeum.integration;

import lombok.Data;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * An encapsulation of Rabbit related properties.
 *
 * @author ioBuilders <tech@io.builders>
 */
@Configuration
@Data
@ConditionalOnProperty(name = "broadcaster.type", havingValue = "RABBIT")
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

  @Value("${rabbitmq.routingKey.messageEvents}")
  private String messageEventsRoutingKey;

  @Bean
  Jackson2JsonMessageConverter producerJackson2MessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
