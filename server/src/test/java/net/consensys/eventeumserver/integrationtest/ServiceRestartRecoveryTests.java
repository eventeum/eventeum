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

import static org.junit.jupiter.api.Assertions.*;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.consensys.eventeum.constant.Constants;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.dto.transaction.TransactionStatus;
import net.consensys.eventeum.model.TransactionIdentifierType;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import net.consensys.eventeum.repository.TransactionMonitoringSpecRepository;
import net.consensys.eventeum.utils.JSON;
import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.web3j.crypto.Hash;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.MethodName.class)
@TestPropertySource(
    properties = {
      "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration"
    })
public abstract class ServiceRestartRecoveryTests extends BaseKafkaIntegrationTest {
  private static final int MONGO_PORT = 27017;

  private static FixedHostPortGenericContainer mongoContainer;

  @Autowired private TransactionMonitoringSpecRepository txRepo;

  @BeforeAll
  public static void startMongo() {
    if (isLocalPortFree(MONGO_PORT)) {
      mongoContainer = new FixedHostPortGenericContainer("mongo:4.0.10");
      mongoContainer.waitingFor(Wait.forListeningPort());
      mongoContainer.withFixedExposedPort(MONGO_PORT, MONGO_PORT);
      mongoContainer.start();

      waitForMongoDBToStart(30000);
    }
  }

  @AfterAll
  public static void stopMongo() {
    if (mongoContainer != null) {
      mongoContainer.stop();
    }
  }

  protected void doBroadcastMissedBlocksOnStartupAfterFailureTest() throws Exception {

    triggerBlocks(5);

    waitForBlockMessages(5);

    // Depending on timing, the genesis block is sometimes broadcast,
    // So wait another few seconds for the last block if this is the case
    waitForBroadcast();

    List<BlockDetails> broadcastBlocks = getBroadcastBlockMessages();

    System.out.println("BROADCAST BLOCKS BEFORE: " + JSON.stringify(getBroadcastBlockMessages()));

    // Ensure latest block has been updated in eventeum
    waitForBroadcast();

    TestContextManager tc = new TestContextManager(getClass());
    tc.prepareTestInstance(this);

    AtomicReference<BigInteger> lastBlockNumber = new AtomicReference<>(BigInteger.ZERO);

    restartEventeumKafka(
        () -> {
          lastBlockNumber.set(broadcastBlocks.get(broadcastBlocks.size() - 1).getNumber());
          getBroadcastBlockMessages().clear();
        },
        tc);

    triggerBlocks(2);

    Thread.sleep(2000);
    triggerBlocks(1);

    waitForBlockMessages(3);

    System.out.println("BROADCAST BLOCKS AFTER: " + JSON.stringify(getBroadcastBlockMessages()));

    System.err.println(
        "LAST BLOCK: "
            + lastBlockNumber.get()
            + " FIRST BLOCK AFTER RESTART: "
            + getBroadcastBlockMessages().get(0).getNumber());
    // Eventeum will rebroadcast the last seen block after restart in case block
    // wasn't fully processed (when numBlocksToReplay=0)
    assertTrue(
        lastBlockNumber.get().intValue()
                == getBroadcastBlockMessages().get(0).getNumber().intValue()
            || lastBlockNumber.get().intValue() - 1
                == getBroadcastBlockMessages().get(0).getNumber().intValue()
            || lastBlockNumber.get().intValue() + 1
                == getBroadcastBlockMessages().get(0).getNumber().intValue());

    // Assert incremental blocks
    for (int i = 0; i < getBroadcastBlockMessages().size(); i++) {
      final BigInteger expectedNumber = BigInteger.valueOf(i + lastBlockNumber.get().intValue());

      assertTrue(
          expectedNumber.intValue() == getBroadcastBlockMessages().get(i).getNumber().intValue()
              || expectedNumber.intValue() - 1
                  == getBroadcastBlockMessages().get(i).getNumber().intValue()
              || expectedNumber.intValue() + 1
                  == getBroadcastBlockMessages().get(i).getNumber().intValue());
    }
  }

  public void doBroadcastUnconfirmedEventAfterFailureTest() throws Exception {

    final EventEmitter emitter = deployEventEmitterContract();

    final ContractEventFilter registeredFilter =
        registerDummyEventFilter(emitter.getContractAddress());

    TestContextManager tc = new TestContextManager(getClass());
    tc.prepareTestInstance(this);

    restartEventeumKafka(
        () -> {
          try {
            emitter.emitEvent(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();
            waitForBroadcast();
          } catch (Exception e) {
            e.printStackTrace();
            fail("Unable to emit event");
          }
        },
        tc);

    waitForContractEventMessages(1);

    assertEquals(1, getBroadcastContractEvents().size());

    final ContractEventDetails eventDetails = getBroadcastContractEvents().get(0);
    verifyDummyEventDetails(registeredFilter, eventDetails, ContractEventStatus.UNCONFIRMED);
  }

  public void doBroadcastConfirmedEventAfter12BlocksWhenDownTest() throws Exception {

    final EventEmitter emitter = deployEventEmitterContract();

    final ContractEventFilter registeredFilter =
        registerDummyEventFilter(emitter.getContractAddress());

    TestContextManager tc = new TestContextManager(getClass());
    tc.prepareTestInstance(this);

    restartEventeumKafka(
        () -> {
          try {
            try {
              emitter.emitEvent(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();
              waitForBroadcast();

              triggerBlocks(12);
            } catch (Exception e) {
              e.printStackTrace();
            }
          } catch (Exception e) {
            e.printStackTrace();
            fail("Unable to emit event");
          }
        },
        tc);

    waitForContractEventMessages(1);

    assertEquals(1, getBroadcastContractEvents().size());

    verifyDummyEventDetails(
        registeredFilter, getBroadcastContractEvents().get(0), ContractEventStatus.CONFIRMED);
  }

  protected void doBroadcastTransactionUnconfirmedAfterFailureTest() throws Exception {

    triggerBlocks(1);

    waitForBlockMessages(1);

    // We're going to send 10 transactions in front to trigger blocks so nonce should be 10 higher
    final BigInteger nonce = getNonce().add(BigInteger.TEN);

    final String signedHex = createRawSignedTransactionHex(nonce);

    final String txHash = Hash.sha3(signedHex);

    TransactionMonitoringSpec monitorSpec =
        new TransactionMonitoringSpec(
            TransactionIdentifierType.HASH, txHash, Constants.DEFAULT_NODE_NAME);

    monitorTransaction(monitorSpec);

    txRepo.findAll();

    TestContextManager tc = new TestContextManager(getClass());
    tc.prepareTestInstance(this);

    restartEventeumKafka(
        () -> {
          try {
            triggerBlocks(10);
            final String actualTxHash = sendRawTransaction(signedHex);
            assertEquals(txHash, actualTxHash);
            waitForBroadcast();

            triggerBlocks(10);
          } catch (Exception e) {
            e.printStackTrace();
          }
        },
        tc);

    txRepo.findAll();

    waitForTransactionMessages(1);

    assertEquals(1, getBroadcastTransactionMessages().size());

    final TransactionDetails txDetails = getBroadcastTransactionMessages().get(0);
    assertEquals(txHash, txDetails.getHash());
    assertEquals(TransactionStatus.UNCONFIRMED, txDetails.getStatus());
  }

  private static void waitForMongoDBToStart(long timeToWait) {
    final long startTime = System.currentTimeMillis();

    while (true) {
      if (System.currentTimeMillis() > startTime + timeToWait) {
        throw new IllegalStateException("MongoDB failed to start...");
      }

      try {
        // Check mongo is up
        final MongoClient mongo = MongoClients.create();
        final List<String> databaseNames = IterableUtils.toList(mongo.listDatabaseNames());

        if (databaseNames.size() > 0) {
          break;
        }
      } catch (Throwable t) {
        // If an error occurs, mongoDB is not yet up
      }

      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
        ;
      }
    }
  }
}
