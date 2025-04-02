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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.model.EventFilterSyncStatus;
import net.consensys.eventeum.model.SyncStatus;
import net.consensys.eventeum.repository.EventFilterSyncStatusRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class BaseEventCatchupTest extends BaseKafkaIntegrationTest {

  private static final int NUM_OF_EVENTS_BEFORE_START = 30;

  private static EventEmitter eventEmitter;

  static {
    BaseIntegrationTest.shouldPersistNodeVolume = false;
  }

  @Autowired private EventFilterSyncStatusRepository syncStatusRepository;

  @BeforeAll
  public static void doEmitEvents() throws Exception {
    final Web3j web3j = Web3j.build(new HttpService("http://localhost:8545"));

    eventEmitter = EventEmitter.deploy(web3j, CREDS, GAS_PRICE, GAS_LIMIT).send();

    for (int i = 0; i < NUM_OF_EVENTS_BEFORE_START; i++) {
      eventEmitter.emitEvent(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();
    }

    System.setProperty("EVENT_EMITTER_CONTRACT_ADDRESS", eventEmitter.getContractAddress());
  }

  @BeforeEach
  @Override
  public void clearMessages() {
    // Theres a race condition that sometimes causes the event messages to be cleared after being
    // received
    // Overriding to remove the clearing of event messages as its not required here (until there are
    // multiple tests!)
    getBroadcastBlockMessages().clear();
    getBroadcastTransactionMessages().clear();
  }

  @Test
  public void testEventsCatchupOnStart() throws Exception {
    waitForMessages(30, getBroadcastContractEvents());

    final List<ContractEventDetails> events = getBroadcastContractEvents();
    final int startBlock = events.get(0).getBlockNumber().intValue();

    for (int i = 0; i < events.size(); i++) {
      assertEquals(startBlock + i, events.get(i).getBlockNumber().intValue());
    }

    final EventFilterSyncStatus syncStatus =
        syncStatusRepository
            .findById("DummyEvent")
            .orElseThrow(() -> new RuntimeException("No sync status in db"));

    assertEquals(SyncStatus.SYNCED, syncStatus.getSyncStatus());

    // Only need to sync with start block
    // assertEquals(events.get(0).getBlockNumber(), syncStatus.getLastBlockNumber());

    getBroadcastContractEvents().clear();

    eventEmitter.emitEvent(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();

    waitForBlockMessages(1);

    final ContractEventDetails event = getBroadcastContractEvents().get(0);
    assertEquals(startBlock + NUM_OF_EVENTS_BEFORE_START + 1, event.getBlockNumber().intValue());
  }

  @Override
  protected Map<String, Object> modifyKafkaConsumerProps(Map<String, Object> consumerProps) {
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    return consumerProps;
  }
}
