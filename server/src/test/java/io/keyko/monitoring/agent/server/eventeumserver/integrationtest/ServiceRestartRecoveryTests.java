package io.keyko.monitoring.agent.server.eventeumserver.integrationtest;

import com.mongodb.MongoClient;
import junit.framework.TestCase;
import io.keyko.monitoring.agent.core.constant.Constants;
import io.keyko.monitoring.agent.core.dto.block.BlockDetails;
import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.dto.event.ContractEventStatus;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionDetails;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionStatus;
import io.keyko.monitoring.agent.core.model.TransactionIdentifierType;
import io.keyko.monitoring.agent.core.model.TransactionMonitoringSpec;
import io.keyko.monitoring.agent.core.repository.TransactionMonitoringSpecRepository;
import io.keyko.monitoring.agent.core.utils.JSON;
import io.keyko.monitoring.agent.server.eventeumserver.integrationtest.utils.RestartingSpringRunner;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.web3j.crypto.Hash;
import wiremock.org.apache.commons.collections4.IterableUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(RestartingSpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@TestPropertySource(properties=
        {"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration"})
public abstract class ServiceRestartRecoveryTests extends BaseKafkaIntegrationTest {
    private static final int MONGO_PORT = 27017;

    private static FixedHostPortGenericContainer mongoContainer;

    @Autowired
    private TransactionMonitoringSpecRepository txRepo;

    @BeforeClass
    public static void startMongo() {
        mongoContainer = new FixedHostPortGenericContainer("mongo:3.5.5");
        mongoContainer.waitingFor(Wait.forListeningPort());
        mongoContainer.withFixedExposedPort(MONGO_PORT, MONGO_PORT);
        mongoContainer.start();

        waitForMongoDBToStart(30000);
    }

    @AfterClass
    public static void stopMongo() {
        if (mongoContainer != null) {
            mongoContainer.stop();
        }
    }

    protected void doBroadcastMissedBlocksOnStartupAfterFailureTest() throws Exception {

        triggerBlocks(5);

        waitForBlockMessages(5);

        //Depending on timing, the genesis block is sometimes broadcast,
        //So wait another few seconds for the last block if this is the case
        waitForBroadcast();

        List<BlockDetails> broadcastBlocks = getBroadcastBlockMessages();

        System.out.println("BROADCAST BLOCKS BEFORE: " + JSON.stringify(getBroadcastBlockMessages()));

        final BigInteger lastBlockNumber = broadcastBlocks.get(broadcastBlocks.size() - 1).getNumber();

        //Ensure latest block has been updated in eventeum
        waitForBroadcast();

        getBroadcastBlockMessages().clear();

        restartEventeum(() -> {
            try {
                triggerBlocks(4);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        triggerBlocks(2);

        waitForBlockMessages(7);

        System.out.println("BROADCAST BLOCKS AFTER: " + JSON.stringify(getBroadcastBlockMessages()));

        //Eventeum will rebroadcast the last seen block after restart in case block
        //wasn't fully processed
        assertEquals(lastBlockNumber, getBroadcastBlockMessages().get(0).getNumber());

        //Assert incremental blocks
        for(int i = 0; i < getBroadcastBlockMessages().size(); i++) {
            final BigInteger expectedNumber = BigInteger.valueOf(i + lastBlockNumber.intValue());

            assertEquals(expectedNumber, getBroadcastBlockMessages().get(i).getNumber());
        }
    }

    public void doBroadcastUnconfirmedEventAfterFailureTest() throws Exception {

        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter registeredFilter = registerDummyEventFilter(emitter.getContractAddress());

        restartEventeum(() -> {
            try {
                emitter.emitEvent(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();
                waitForBroadcast();
            } catch (Exception e) {
                e.printStackTrace();
                TestCase.fail("Unable to emit event");
            }
        });

        waitForContractEventMessages(1);

        assertEquals(1, getBroadcastContractEvents().size());

        final ContractEventDetails eventDetails = getBroadcastContractEvents().get(0);
        verifyDummyEventDetails(registeredFilter, eventDetails, ContractEventStatus.UNCONFIRMED);
    }

    public void doBroadcastConfirmedEventAfter12BlocksWhenDownTest() throws Exception {

        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter registeredFilter = registerDummyEventFilter(emitter.getContractAddress());

        restartEventeum(() -> {
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
                TestCase.fail("Unable to emit event");
            }
        });

        waitForContractEventMessages(2);

        assertEquals(2, getBroadcastContractEvents().size());

        verifyDummyEventDetails(registeredFilter,
                getBroadcastContractEvents().get(0), ContractEventStatus.UNCONFIRMED);

        verifyDummyEventDetails(registeredFilter,
                getBroadcastContractEvents().get(1), ContractEventStatus.CONFIRMED);
    }

    protected void doBroadcastTransactionUnconfirmedAfterFailureTest() throws Exception {

        triggerBlocks(1);

        waitForBlockMessages(1);

        //We're going to send 10 transactions in front to trigger blocks so nonce should be 10 higher
        final BigInteger nonce = getNonce().add(BigInteger.TEN);

        final String signedHex = createRawSignedTransactionHex(nonce);

        final String txHash = Hash.sha3(signedHex);

        TransactionMonitoringSpec monitorSpec = new TransactionMonitoringSpec(TransactionIdentifierType.HASH, txHash, Constants.DEFAULT_NODE_NAME);

        monitorTransaction(monitorSpec);

        txRepo.findAll();

        restartEventeum(() -> {
            try {
                triggerBlocks(10);
                final String actualTxHash = sendRawTransaction(signedHex);
                assertEquals(txHash, actualTxHash);
                waitForBroadcast();

                triggerBlocks(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

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
                //Check mongo is up
                final MongoClient mongo = new MongoClient();
                final List<String> databaseNames = IterableUtils.toList(mongo.listDatabaseNames());

                if (databaseNames.size() > 0) {
                    break;
                }
            } catch (Throwable t) {
                //If an error occurs, mongoDB is not yet up
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();;
            }
        }
    }
}
