package net.consensys.eventeumserver.integrationtest;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.dto.transaction.TransactionStatus;
import net.consensys.eventeum.integration.eventstore.EventStore;
import net.consensys.eventeum.model.LatestBlock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:application-test-db.properties")
public class BroadcasterDBEventStoreIT extends MainBroadcasterTests {

    @Autowired
    private EventStore eventStore;

    @Test
    public void testRegisterEventFilterSavesFilterInDb() {
        doTestRegisterEventFilterSavesFilterInDb();
    }

    @Test
    public void testRegisterEventFilterBroadcastsAddedMessage() throws InterruptedException {
        doTestRegisterEventFilterBroadcastsAddedMessage();
    }

    @Test
    public void testRegisterEventFilterReturnsCorrectId() {
        doTestRegisterEventFilterReturnsCorrectId();
    }

    @Test
    public void testRegisterEventFilterReturnsCreatedIdWhenNotSet() {
        doTestRegisterEventFilterReturnsCreatedIdWhenNotSet();
    }

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
    public void testUnregisterNonExistentFilter() {
        doTestUnregisterNonExistentFilter();
    }

    @Test
    public void testUnregisterEventFilterDeletesFilterInDb() {
        doTestUnregisterEventFilterDeletesFilterInDb();
    }

    @Test
    public void testUnregisterEventFilterBroadcastsRemovedMessage() throws InterruptedException {
        doTestUnregisterEventFilterBroadcastsRemovedMessage();
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
    public void testBroadcastEventAddedToEventStore() throws Exception {

        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter registeredFilter = registerDummyEventFilter(emitter.getContractAddress());
        emitter.emit(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();

        waitForContractEventMessages(1);

        assertEquals(1, getBroadcastContractEvents().size());

        final ContractEventDetails eventDetails = getBroadcastContractEvents().get(0);

        Thread.sleep(1000);

        List<ContractEventDetails> savedEvents = eventStore.getContractEventsForSignature(
            eventDetails.getEventSpecificationSignature(), PageRequest.of(0, 100000)).getContent();

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
