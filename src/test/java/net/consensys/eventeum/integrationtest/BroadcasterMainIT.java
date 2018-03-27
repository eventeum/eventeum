package net.consensys.eventeum.integrationtest;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.ContractEventFilterAdded;
import net.consensys.eventeum.dto.message.ContractEventFilterRemoved;
import net.consensys.eventeum.dto.message.Message;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:application-test.properties")
public class BroadcasterMainIT extends BaseIntegrationTest {

    @Test
    public void testRegisterEventFilterSavesFilterInDb() {
        final ContractEventFilter registeredFilter = registerDummyEventFilter(FAKE_CONTRACT_ADDRESS);

        final ContractEventFilter saved = getFilterRepo().findOne(getDummyEventFilterId());
        assertEquals(registeredFilter, saved);
    }

    @Test
    public void testRegisterEventFilterBroadcastsAddedMessage() throws InterruptedException {
        final ContractEventFilter registeredFilter = registerDummyEventFilter(FAKE_CONTRACT_ADDRESS);

        waitForBroadcast();
        assertEquals(1, getBroadcastFilterEventMessages().size());

        final Message<ContractEventFilter> broadcastMessage = getBroadcastFilterEventMessages().get(0);

        assertEquals(true, broadcastMessage instanceof ContractEventFilterAdded);
        assertEquals(registeredFilter, broadcastMessage.getDetails());
    }

    @Test
    public void testBroadcastsUnconfirmedEventAfterInitialEmit() throws Exception {

        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter registeredFilter = registerDummyEventFilter(emitter.getContractAddress());
        emitter.emit(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();

        waitForContractEventMessages(1);

        assertEquals(1, getBroadcastContractEventMessages().size());

        final Message<ContractEventDetails> message = getBroadcastContractEventMessages().get(0);
        verifyDummyEventDetails(registeredFilter, message.getDetails(), ContractEventStatus.UNCONFIRMED);
    }

    @Test
    public void testBroadcastsConfirmedEventAfterBlockThresholdReached() throws Exception {

        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter registeredFilter = registerDummyEventFilter(emitter.getContractAddress());
        emitter.emit(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();

        waitForFilterPoll();
        triggerBlocks(12);
        waitForContractEventMessages(2);

        assertEquals(2, getBroadcastContractEventMessages().size());

        final Message<ContractEventDetails> message = getBroadcastContractEventMessages().get(1);
        verifyDummyEventDetails(registeredFilter, message.getDetails(), ContractEventStatus.CONFIRMED);
    }

    @Test
    public void testUnregisterNonExistentFilter() {
        try {
            unregisterEventFilter("NonExistent");
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
        }
    }

    @Test
    public void testUnregisterEventFilterDeletesFilterInDb() {
        final ContractEventFilter registeredFilter = registerDummyEventFilter(FAKE_CONTRACT_ADDRESS);

        ContractEventFilter saved = getFilterRepo().findOne(getDummyEventFilterId());
        assertEquals(registeredFilter, saved);

        unregisterDummyEventFilter();

        saved = getFilterRepo().findOne(getDummyEventFilterId());
        assertNull(saved);
    }

    @Test
    public void testUnregisterEventFilterBroadcastsRemovedMessage() throws InterruptedException {
        final ContractEventFilter registeredFilter = doRegisterAndUnregister(FAKE_CONTRACT_ADDRESS);

        waitForBroadcast();
        assertEquals(2, getBroadcastFilterEventMessages().size());

        final Message<ContractEventFilter> broadcastMessage = getBroadcastFilterEventMessages().get(1);

        assertEquals(true, broadcastMessage instanceof ContractEventFilterRemoved);
        assertEquals(registeredFilter, broadcastMessage.getDetails());
    }

    @Test
    public void testContractEventForUnregisteredEventFilterNotBroadcast() throws Exception {
        final EventEmitter emitter = deployEventEmitterContract();
        doRegisterAndUnregister(emitter.getContractAddress());
        emitter.emit(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();

        waitForBroadcast();
        assertEquals(0, getBroadcastContractEventMessages().size());
    }

    private ContractEventFilter doRegisterAndUnregister(String contractAddress) throws InterruptedException {
        final ContractEventFilter registeredFilter = registerDummyEventFilter(contractAddress);
        ContractEventFilter saved = getFilterRepo().findOne(getDummyEventFilterId());
        assertEquals(registeredFilter, saved);

        unregisterDummyEventFilter();

        saved = getFilterRepo().findOne(getDummyEventFilterId());
        assertNull(saved);

        return registeredFilter;
    }
}
