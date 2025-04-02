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

package net.consensys.eventeum.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.EventeumMessage;
import net.consensys.eventeum.integration.KafkaSettings;
import net.consensys.eventeum.integration.PulsarSettings;
import net.consensys.eventeum.integration.RabbitSettings;
import net.consensys.eventeum.integration.broadcast.blockchain.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;

/**
 * Spring bean configuration for the BlockchainEventBroadcaster.
 *
 * <p>Registers a broadcaster bean based on the value of the broadcaster.type property.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Configuration
public class BlockchainEventBroadcasterConfiguration {

  private static final String EXPIRATION_PROPERTY = "${broadcaster.cache.expirationMillis}";
  private static final String BROADCASTER_PROPERTY = "broadcaster.type";
  private static final String ENABLE_BLOCK_NOTIFICATIONS =
      "${broadcaster.enableBlockNotifications:true}";

  private Long onlyOnceCacheExpirationTime;
  private boolean enableBlockNotifications;

  @Autowired
  public BlockchainEventBroadcasterConfiguration(
      @Value(EXPIRATION_PROPERTY) Long onlyOnceCacheExpirationTime,
      @Value(ENABLE_BLOCK_NOTIFICATIONS) boolean enableBlockNotifications) {
    this.onlyOnceCacheExpirationTime = onlyOnceCacheExpirationTime;
    this.enableBlockNotifications = enableBlockNotifications;
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(name = BROADCASTER_PROPERTY, havingValue = "KAFKA")
  public BlockchainEventBroadcaster kafkaBlockchainEventBroadcaster(
      KafkaTemplate<String, EventeumMessage> kafkaTemplate,
      KafkaSettings kafkaSettings,
      CrudRepository<ContractEventFilter, String> filterRepository) {
    final BlockchainEventBroadcaster broadcaster =
        new KafkaBlockchainEventBroadcaster(kafkaTemplate, kafkaSettings, filterRepository);

    return onlyOnceWrap(broadcaster);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(name = BROADCASTER_PROPERTY, havingValue = "HTTP")
  public BlockchainEventBroadcaster httpBlockchainEventBroadcaster(
      HttpBroadcasterSettings settings,
      @Qualifier("eternalRetryTemplate") RetryTemplate retryTemplate) {
    final BlockchainEventBroadcaster broadcaster =
        new HttpBlockchainEventBroadcaster(settings, retryTemplate);

    return onlyOnceWrap(broadcaster);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(name = BROADCASTER_PROPERTY, havingValue = "RABBIT")
  public BlockchainEventBroadcaster rabbitBlockChainEventBroadcaster(
      RabbitTemplate rabbitTemplate, RabbitSettings rabbitSettings) {
    final BlockchainEventBroadcaster broadcaster =
        new RabbitBlockChainEventBroadcaster(rabbitTemplate, rabbitSettings);

    return onlyOnceWrap(broadcaster);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(name = BROADCASTER_PROPERTY, havingValue = "PULSAR")
  public BlockchainEventBroadcaster pulsarBlockChainEventBroadcaster(
      PulsarSettings settings, ObjectMapper mapper) {
    final BlockchainEventBroadcaster broadcaster;
    try {
      broadcaster = new PulsarBlockChainEventBroadcaster(settings, mapper);
    } catch (Exception e) {
      throw new BeanCreationException("Error creating pulsar broadcaster", e);
    }

    return onlyOnceWrap(broadcaster);
  }

  private BlockchainEventBroadcaster onlyOnceWrap(BlockchainEventBroadcaster toWrap) {
    return new EventBroadcasterWrapper(
        onlyOnceCacheExpirationTime, toWrap, enableBlockNotifications);
  }
}
