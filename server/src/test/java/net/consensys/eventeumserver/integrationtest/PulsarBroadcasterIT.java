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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.integration.PulsarSettings;
import net.consensys.eventeumserver.integrationtest.container.PulsarContainer;
import org.apache.pulsar.client.api.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:application-test-db-pulsar.properties")
public class PulsarBroadcasterIT extends BroadcasterSmokeTest {

  private static PulsarContainer pulsarContainer;

  private PulsarClient client;

  @Autowired private PulsarSettings settings;

  private BackgroundPulsarConsumer<BlockDetails> blockBackgroundConsumer;

  private BackgroundPulsarConsumer<ContractEventDetails> eventBackgroundConsumer;

  private BackgroundPulsarConsumer<TransactionDetails> transactionBackgroundConsumer;

  @BeforeAll
  public static void setup() {
    pulsarContainer = new PulsarContainer();
    pulsarContainer.start();

    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    System.setProperty("PULSAR_URL", pulsarContainer.getPlainTextServiceUrl());
  }

  @AfterAll
  public static void tearDown() {
    pulsarContainer.stop();

    System.clearProperty("PULSAR_URL");
  }

  @BeforeEach
  public void configureConsumer() throws PulsarClientException {
    client = PulsarClient.builder().serviceUrl(pulsarContainer.getPlainTextServiceUrl()).build();

    blockBackgroundConsumer =
        new BackgroundPulsarConsumer<>(
            createConsumer(settings.getTopic().getBlockEvents()), BlockDetails.class);
    blockBackgroundConsumer.start(block -> onBlockMessageReceived(block));

    eventBackgroundConsumer =
        new BackgroundPulsarConsumer<>(
            createConsumer(settings.getTopic().getContractEvents()), ContractEventDetails.class);
    eventBackgroundConsumer.start(event -> onContractEventMessageReceived(event));

    transactionBackgroundConsumer =
        new BackgroundPulsarConsumer<>(
            createConsumer(settings.getTopic().getTransactionEvents()), TransactionDetails.class);
    transactionBackgroundConsumer.start(event -> onTransactionMessageReceived(event));
  }

  @AfterEach
  public void teardownConsumers() throws PulsarClientException {
    blockBackgroundConsumer.stop();
    eventBackgroundConsumer.stop();
    client.close();
  }

  private Consumer<byte[]> createConsumer(String topic) throws PulsarClientException {
    return client
        .newConsumer()
        .topic(topic)
        .subscriptionName("test-" + topic)
        .ackTimeout(10, TimeUnit.SECONDS)
        .subscriptionType(SubscriptionType.Exclusive)
        .subscribe();
  }

  private class BackgroundPulsarConsumer<T> {
    private final Consumer<byte[]> pulsarConsumer;

    private final Class<T> entityClass;

    private final ExecutorService executerService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private boolean stopped;

    private BackgroundPulsarConsumer(Consumer<byte[]> pulsarConsumer, Class<T> entityClass) {
      this.pulsarConsumer = pulsarConsumer;
      this.entityClass = entityClass;

      executerService = Executors.newCachedThreadPool();
    }

    public void start(java.util.function.Consumer<T> consumer) {

      executerService.execute(
          () -> {
            do {
              try {
                // Wait until a message is available
                Message<byte[]> msg = pulsarConsumer.receive();

                consumer.accept(objectMapper.readValue(msg.getValue(), entityClass));

                // Acknowledge processing of the message so that it can be deleted
                pulsarConsumer.acknowledge(msg);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            } while (!stopped);
          });
    }

    public void stop() throws PulsarClientException {
      stopped = true;

      pulsarConsumer.close();
    }
  }
}
