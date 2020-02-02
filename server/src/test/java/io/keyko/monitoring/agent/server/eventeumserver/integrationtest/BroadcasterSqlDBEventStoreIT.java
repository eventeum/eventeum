package io.keyko.monitoring.agent.server.eventeumserver.integrationtest;

import io.keyko.monitoring.agent.core.dto.block.BlockDetails;
import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import io.keyko.monitoring.agent.core.integration.eventstore.EventStore;
import io.keyko.monitoring.agent.core.model.LatestBlock;
import io.keyko.monitoring.agent.core.utils.JSON;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.web3j.crypto.Keys;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:application-test-sql.properties")
public class BroadcasterSqlDBEventStoreIT extends MainBroadcasterTests {

    @ClassRule
    public static final GenericContainer sqlServerContainer =
            new FixedHostPortGenericContainer("microsoft/mssql-server-linux")
                    .withFixedExposedPort(1433, 1433)
                    .withEnv("ACCEPT_EULA", "Y")
                    .withEnv("SA_PASSWORD", "reallyStrongPwd123")
                    .waitingFor(Wait.forListeningPort());

    @Autowired
    private EventStore eventStore;

    @Test
    public void testBroadcastsUnconfirmedEventAfterInitialEmit() throws Exception {
        doTestBroadcastsUnconfirmedEventAfterInitialEmit();
    }

    @Test
    public void testBroadcastNotOrderedEvent() throws Exception {
        doTestBroadcastsNotOrderedEvent();
    }

    @Test
    public void testBroadcastsConfirmedEventAfterBlockThresholdReached() throws Exception {
        doTestBroadcastsConfirmedEventAfterBlockThresholdReached();
    }

    @Test
    public void testContractEventForUnregisteredEventFilterNotBroadcast() throws Exception {
        doTestContractEventForUnregisteredEventFilterNotBroadcast();
    }

    @Test
    public void testBroadcastBlock() throws Exception {
        doTestBroadcastBlock();
    }

    @Test
    public void testBroadcastsUnconfirmedTransactionAfterInitialMining() throws Exception {
        doTestBroadcastsUnconfirmedTransactionAfterInitialMining();
    }

    @Test
    public void testBroadcastsConfirmedTransactionAfterBlockThresholdReached() throws Exception {
        doTestBroadcastsConfirmedTransactionAfterBlockThresholdReached();
    }

    @Test
    public void testBroadcastFailedTransactionFilteredByHash() throws Exception {
        doTestBroadcastFailedTransactionFilteredByHash();
    }

    @Test
    public void testBroadcastFailedTransactionFilteredByTo() throws Exception {
        doTestBroadcastFailedTransactionFilteredByTo();
    }

    @Test
    public void testBroadcastFailedTransactionFilteredByFrom() throws Exception {
        doTestBroadcastFailedTransactionFilteredByFrom();
    }

    @Test
    public void testBroadcastEventAddedToEventStore() throws Exception {

        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter registeredFilter = registerDummyEventFilter(emitter.getContractAddress());
        emitter.emitEvent(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();

        waitForContractEventMessages(1);

        assertEquals("***** " + JSON.stringify(getBroadcastContractEvents()),1, getBroadcastContractEvents().size());

        final ContractEventDetails eventDetails = getBroadcastContractEvents().get(0);

        Thread.sleep(1000);

        List<ContractEventDetails> savedEvents = eventStore.getContractEventsForSignature(
            eventDetails.getEventSpecificationSignature(), Keys.toChecksumAddress(emitter.getContractAddress()), PageRequest.of(0, 100000)).getContent();

        assertEquals(1, savedEvents.size());
        assertEquals(eventDetails, savedEvents.get(0));
    }

    @Test
    public void testBroadcastBlockAddedToEventStore() throws Exception {
        doTestBroadcastBlock();

        Thread.sleep(1000);

        final Optional<LatestBlock> latestBlock = eventStore.getLatestBlockForNode("default");

        assertEquals(true, latestBlock.isPresent());

        final List<BlockDetails> broadcastBlocks = getBroadcastBlockMessages();
        assertEquals(broadcastBlocks.get(broadcastBlocks.size() - 1).getNumber(),
                latestBlock.get().getNumber());
    }
}
