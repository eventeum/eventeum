package net.consensys.eventeumserver.integrationtest;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.ContractEventFilterAdded;
import net.consensys.eventeum.dto.message.ContractEventFilterRemoved;
import net.consensys.eventeum.dto.message.EventeumMessage;
import net.consensys.eventeum.dto.message.TransactionMonitorAdded;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.dto.transaction.TransactionStatus;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import net.consensys.eventeum.utils.JSON;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.web3j.crypto.Hash;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

public abstract class MainBroadcasterTests extends BaseKafkaIntegrationTest {

    public void doTestBroadcastsUnconfirmedEventAfterInitialEmit() throws Exception {

        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter registeredFilter = registerDummyEventFilter(emitter.getContractAddress());
        emitter.emitEvent(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();

        waitForContractEventMessages(1);

        assertEquals(1, getBroadcastContractEvents().size());

        final ContractEventDetails eventDetails = getBroadcastContractEvents().get(0);
        verifyDummyEventDetails(registeredFilter, eventDetails, ContractEventStatus.UNCONFIRMED);
    }

    public void doTestBroadcastsNotOrderedEvent() throws Exception {
        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter filter = createDummyEventNotOrderedFilter(emitter.getContractAddress());
        final ContractEventFilter registeredFilter = registerEventFilter(filter);
        emitter.emitEventNotOrdered(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();

        waitForContractEventMessages(1);

        assertEquals(1, getBroadcastContractEvents().size());

        final ContractEventDetails eventDetails = getBroadcastContractEvents().get(0);
        verifyDummyEventDetails(registeredFilter, eventDetails, ContractEventStatus.UNCONFIRMED);
    }

    public void doTestBroadcastsConfirmedEventAfterBlockThresholdReached() throws Exception {

        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter registeredFilter = registerDummyEventFilter(emitter.getContractAddress());
        emitter.emitEvent(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();

        waitForFilterPoll();
        triggerBlocks(12);
        waitForContractEventMessages(2);

        assertEquals(2, getBroadcastContractEvents().size());

        final ContractEventDetails eventDetails = getBroadcastContractEvents().get(1);
        verifyDummyEventDetails(registeredFilter, eventDetails, ContractEventStatus.CONFIRMED);
    }

    public void doTestContractEventForUnregisteredEventFilterNotBroadcast() throws Exception {
        final EventEmitter emitter = deployEventEmitterContract();
        final ContractEventFilter filter = doRegisterAndUnregister(emitter.getContractAddress());
        emitter.emitEvent(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();

        waitForBroadcast();

        //For some reason events are sometimes consumed for old tests on circleci
        //Allow events as long as they aren't for this tests registered filter
        if (getBroadcastContractEvents().size() > 0) {
            getBroadcastContractEvents().forEach(
                    event -> assertNotEquals(filter.getId(), event.getFilterId()));
        }
    }

    public void doTestBroadcastBlock() throws Exception {
        triggerBlocks(1);

        waitForBlockMessages(1);

        Assert.assertTrue("No blocks received", getBroadcastBlockMessages().size() >= 1);

        BlockDetails blockDetails = getBroadcastBlockMessages().get(0);
        assertEquals(1, blockDetails.getNumber().compareTo(BigInteger.ZERO));
        assertNotNull(blockDetails.getHash());
    }

    public String doTestBroadcastsUnconfirmedTransactionAfterInitialMining() throws Exception {

        final String signedTxHex = createRawSignedTransactionHex();
        final String txHash = Hash.sha3(signedTxHex);

        monitorTransaction(txHash);

        assertEquals(txHash, sendRawTransaction(signedTxHex));

        waitForTransactionMessages(1);

        assertEquals(1, getBroadcastTransactionMessages().size());

        final TransactionDetails txDetails = getBroadcastTransactionMessages().get(0);
        assertEquals(txHash, txDetails.getHash());
        assertEquals(TransactionStatus.UNCONFIRMED, txDetails.getStatus());

        return txHash;
    }

    public void doTestBroadcastsConfirmedTransactionAfterBlockThresholdReached() throws Exception {

        final String txHash = doTestBroadcastsUnconfirmedTransactionAfterInitialMining();

        triggerBlocks(12);
        waitForTransactionMessages(2);

        assertEquals(2, getBroadcastTransactionMessages().size());
        final TransactionDetails txDetails = getBroadcastTransactionMessages().get(1);
        assertEquals(txHash, txDetails.getHash());
        assertEquals(TransactionStatus.CONFIRMED, txDetails.getStatus());
    }
}
