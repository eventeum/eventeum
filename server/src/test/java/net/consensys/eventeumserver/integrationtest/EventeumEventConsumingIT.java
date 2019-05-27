package net.consensys.eventeumserver.integrationtest;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.dto.transaction.TransactionStatus;
import net.consensys.eventeum.integration.broadcast.internal.KafkaEventeumEventBroadcaster;
import net.consensys.eventeum.model.TransactionIdentifierType;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.crypto.Hash;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:application-test-db.properties")
public class EventeumEventConsumingIT extends BaseKafkaIntegrationTest {

    @Autowired
    private KafkaEventeumEventBroadcaster broadcaster;

    @Test
    public void testFilterAddedEventRegistersFilter() throws Exception {

        doBroadcastFilterAddedEventAndVerifyRegistered(deployEventEmitterContract());
    }

    @Test
    public void testFilterRemovedEventRemovesFilter() throws Exception {
        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter filter = doBroadcastFilterAddedEventAndVerifyRegistered(emitter);

        broadcaster.broadcastEventFilterRemoved(filter);

        waitForMessages(2, getBroadcastFilterEventMessages());

        clearMessages();

        emitter.emit(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();

        waitForBroadcast();

        assertEquals(0, getBroadcastContractEvents().size());
    }

    @Test
    public void testTxMonitorAddedEventRegistersMonitor() throws Exception {

        final String signedTxHex = createRawSignedTransactionHex();
        final String txHash = Hash.sha3(signedTxHex);

        final TransactionMonitoringSpec spec = new TransactionMonitoringSpec();
        spec.setNodeName("default");
        spec.setTransactionIdentifier(txHash);
        spec.setType(TransactionIdentifierType.HASH);

        broadcaster.broadcastTransactionMonitorAdded(spec);

        waitForMessages(1, getBroadcastTransactionEventMessages());

        assertEquals(txHash, sendRawTransaction(signedTxHex));

        waitForTransactionMessages(1);

        assertEquals(1, getBroadcastTransactionMessages().size());

        final TransactionDetails txDetails = getBroadcastTransactionMessages().get(0);
        assertEquals(txHash, txDetails.getHash());
        assertEquals(TransactionStatus.UNCONFIRMED, txDetails.getStatus());
    }

    @Test
    public void testTxMonitorRemovedEventRemovesMonitor() throws Exception {

        final String signedTxHex = createRawSignedTransactionHex();
        final String txHash = Hash.sha3(signedTxHex);

        final TransactionMonitoringSpec spec = new TransactionMonitoringSpec();
        spec.setNodeName("default");
        spec.setTransactionIdentifier(txHash);
        spec.setType(TransactionIdentifierType.HASH);

        broadcaster.broadcastTransactionMonitorAdded(spec);

        waitForMessages(1, getBroadcastTransactionEventMessages());

        broadcaster.broadcastTransactionMonitorRemoved(spec);

        waitForMessages(2, getBroadcastTransactionEventMessages());

        assertEquals(txHash, sendRawTransaction(signedTxHex));

        waitForBroadcast();

        assertEquals(0, getBroadcastTransactionMessages().size());
    }

    private ContractEventFilter doBroadcastFilterAddedEventAndVerifyRegistered(
            EventEmitter emitter) throws Exception {

        final ContractEventFilter filter = createDummyEventFilter(emitter.getContractAddress());

        broadcaster.broadcastEventFilterAdded(filter);

        waitForMessages(1, getBroadcastFilterEventMessages());

        emitter.emit(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();

        waitForContractEventMessages(1);

        assertEquals(1, getBroadcastContractEvents().size());

        final ContractEventDetails eventDetails = getBroadcastContractEvents().get(0);
        verifyDummyEventDetails(filter, eventDetails, ContractEventStatus.UNCONFIRMED);

        return filter;
    }
}
