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

package net.consensys.eventeumserver.integrationtest;

import jakarta.annotation.PostConstruct;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.message.EventeumMessage;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:application-test-db-rabbit.properties")
public class RabbitBroadcasterDBEventStoreIT extends BroadcasterSmokeTest {

  public static FixedHostPortGenericContainer rabbitContainer;

  private boolean isFirstTest = true;

  @BeforeEach
  public void waitForRabbitInit() throws InterruptedException {
    // TODO Figure out how to verify when rabbitMQ has started we we don't have to sleep
    if (isFirstTest) {
      Thread.sleep(10000);
    }

    isFirstTest = false;
  }

  @RabbitListener(
      bindings =
          @QueueBinding(
              key = "contractEvents.*",
              value = @Queue("contractEvents"),
              exchange = @Exchange(value = "ThisIsAExchange", type = ExchangeTypes.TOPIC)))
  public void onContractEvent(EventeumMessage message) {
    if (message.getDetails() instanceof ContractEventDetails) {
      onContractEventMessageReceived((ContractEventDetails) message.getDetails());
    } else if (message.getDetails() instanceof TransactionDetails) {
      onTransactionMessageReceived((TransactionDetails) message.getDetails());
    }
  }

  @RabbitListener(
      bindings =
          @QueueBinding(
              key = "transactionEvents.*",
              value = @Queue("transactionEvents"),
              exchange = @Exchange(value = "ThisIsAExchange", type = ExchangeTypes.TOPIC)))
  public void onTransactionEvent(EventeumMessage message) {
    onTransactionMessageReceived((TransactionDetails) message.getDetails());
  }

  @RabbitListener(
      bindings =
          @QueueBinding(
              key = "blockEvents",
              value = @Queue("blockEvents"),
              exchange = @Exchange(value = "ThisIsAExchange", type = ExchangeTypes.TOPIC)))
  public void onBlockEvent(EventeumMessage message) {
    onBlockMessageReceived((BlockDetails) message.getDetails());
  }

  @TestConfiguration
  static class RabbitConfig {

    @PostConstruct
    void initRabbit() {
      if (RabbitBroadcasterDBEventStoreIT.rabbitContainer == null) {
        RabbitBroadcasterDBEventStoreIT.rabbitContainer =
            new FixedHostPortGenericContainer("rabbitmq:3.6.14-management");
        RabbitBroadcasterDBEventStoreIT.rabbitContainer.waitingFor(Wait.forListeningPort());
        RabbitBroadcasterDBEventStoreIT.rabbitContainer.withFixedExposedPort(5672, 5672);
        RabbitBroadcasterDBEventStoreIT.rabbitContainer.start();
      }
    }
  }
}
