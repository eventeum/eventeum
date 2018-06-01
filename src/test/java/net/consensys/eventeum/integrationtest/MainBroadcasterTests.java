package net.consensys.eventeum.integrationtest;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.ContractEventFilterAdded;
import net.consensys.eventeum.dto.message.ContractEventFilterRemoved;
import net.consensys.eventeum.dto.message.Message;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigInteger;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public abstract class MainBroadcasterTests extends BaseIntegrationTest {

    public void doTestRegisterEventFilterSavesFilterInDb() {
        final ContractEventFilter registeredFilter = registerDummyEventFilter(FAKE_CONTRACT_ADDRESS);

        final ContractEventFilter saved = getFilterRepo().findOne(getDummyEventFilterId());
        assertEquals(registeredFilter, saved);
    }

    public void doTestRegisterEventFilterBroadcastsAddedMessage() throws InterruptedException {
        final ContractEventFilter registeredFilter = registerDummyEventFilter(FAKE_CONTRACT_ADDRESS);

        waitForBroadcast();
        assertEquals(1, getBroadcastFilterEventMessages().size());

        final Message<ContractEventFilter> broadcastMessage = getBroadcastFilterEventMessages().get(0);

        assertEquals(true, broadcastMessage instanceof ContractEventFilterAdded);
        assertEquals(registeredFilter, broadcastMessage.getDetails());
    }

    public void doTestRegisterEventFilterReturnsCreatedIdWhenNotSet() {
        final ContractEventFilter filter = createDummyEventFilter(FAKE_CONTRACT_ADDRESS);
        filter.setId(null);

        final ContractEventFilter registeredFilter = registerEventFilter(filter);
        assertNotNull(registeredFilter.getId());

        //This errors if id is not a valid UUID
        UUID.fromString(registeredFilter.getId());
    }

    public void doTestRegisterEventFilterReturnsCorrectId() {
        final ContractEventFilter registeredFilter = registerDummyEventFilter(FAKE_CONTRACT_ADDRESS);

        assertEquals(getDummyEventFilterId(), registeredFilter.getId());
    }

    public void doTestBroadcastsUnconfirmedEventAfterInitialEmit() throws Exception {

        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter registeredFilter = registerDummyEventFilter(emitter.getContractAddress());
        emitter.emit(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();

        waitForContractEventMessages(1);

        assertEquals(1, getBroadcastContractEventMessages().size());

        final Message<ContractEventDetails> message = getBroadcastContractEventMessages().get(0);
        verifyDummyEventDetails(registeredFilter, message.getDetails(), ContractEventStatus.UNCONFIRMED);
    }

    public void doTestBroadcastsNotOrderedEvent() throws Exception {
        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter filter = createDummyEventNotOrderedFilter(emitter.getContractAddress());
        final ContractEventFilter registeredFilter = registerEventFilter(filter);
        emitter.emitNotOrdered(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();

        waitForContractEventMessages(1);

        assertEquals(1, getBroadcastContractEventMessages().size());

        final Message<ContractEventDetails> message = getBroadcastContractEventMessages().get(0);
        verifyDummyEventDetails(registeredFilter, message.getDetails(), ContractEventStatus.UNCONFIRMED);
    }

    public void doTestBroadcastsConfirmedEventAfterBlockThresholdReached() throws Exception {

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

    public void doTestUnregisterNonExistentFilter() {
        try {
            unregisterEventFilter("NonExistent");
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
        }
    }

    public void doTestUnregisterEventFilterDeletesFilterInDb() {
        final ContractEventFilter registeredFilter = registerDummyEventFilter(FAKE_CONTRACT_ADDRESS);

        ContractEventFilter saved = getFilterRepo().findOne(getDummyEventFilterId());
        assertEquals(registeredFilter, saved);

        unregisterDummyEventFilter();

        saved = getFilterRepo().findOne(getDummyEventFilterId());
        assertNull(saved);
    }

    public void doTestUnregisterEventFilterBroadcastsRemovedMessage() throws InterruptedException {
        final ContractEventFilter registeredFilter = doRegisterAndUnregister(FAKE_CONTRACT_ADDRESS);

        waitForBroadcast();
        assertEquals(2, getBroadcastFilterEventMessages().size());

        final Message<ContractEventFilter> broadcastMessage = getBroadcastFilterEventMessages().get(1);

        assertEquals(true, broadcastMessage instanceof ContractEventFilterRemoved);
        assertEquals(registeredFilter, broadcastMessage.getDetails());
    }

    public void doTestContractEventForUnregisteredEventFilterNotBroadcast() throws Exception {
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
