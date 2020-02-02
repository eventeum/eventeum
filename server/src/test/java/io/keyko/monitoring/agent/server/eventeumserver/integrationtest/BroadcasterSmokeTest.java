package io.keyko.monitoring.agent.server.eventeumserver.integrationtest;

import io.keyko.monitoring.agent.core.constant.Constants;
import io.keyko.monitoring.agent.core.dto.block.BlockDetails;
import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.dto.event.ContractEventStatus;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionDetails;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionStatus;
import io.keyko.monitoring.agent.core.model.TransactionIdentifierType;
import io.keyko.monitoring.agent.core.model.TransactionMonitoringSpec;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public abstract class BroadcasterSmokeTest extends BaseIntegrationTest {

    @Test
    public void testBroadcastBlock() throws Exception {
        triggerBlocks(1);

        waitForBlockMessages(1);

        assertTrue("No blocks received", getBroadcastBlockMessages().size() >= 1);

        BlockDetails blockDetails = getBroadcastBlockMessages().get(0);
        assertEquals(1, blockDetails.getNumber().compareTo(BigInteger.ZERO));
        assertNotNull(blockDetails.getHash());
    }

    @Test
    public void testBroadcastContractEvent() throws Exception {

        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter registeredFilter = registerDummyEventFilter(emitter.getContractAddress());
        emitter.emitEvent(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();

        waitForContractEventMessages(1);

        assertEquals(1, getBroadcastContractEvents().size());

        final ContractEventDetails eventDetails = getBroadcastContractEvents().get(0);
        verifyDummyEventDetails(registeredFilter, eventDetails, ContractEventStatus.CONFIRMED);
    }

    @Test
    public void testBroadcastTransactionEvent() throws Exception {

        final String txHash = sendTransaction();
        TransactionMonitoringSpec monitorSpec = new TransactionMonitoringSpec(TransactionIdentifierType.HASH, txHash, Constants.DEFAULT_NODE_NAME);

        monitorTransaction(monitorSpec);

        waitForTransactionMessages(1);

        assertEquals(1, getBroadcastTransactionMessages().size());

        final TransactionDetails txDetails = getBroadcastTransactionMessages().get(0);
        assertEquals(txHash, txDetails.getHash());
        assertEquals(TransactionStatus.CONFIRMED, txDetails.getStatus());
    }

    protected void onBlockMessageReceived(BlockDetails block) {
        getBroadcastBlockMessages().add(block);
    }

    protected void onContractEventMessageReceived(ContractEventDetails event) {
        getBroadcastContractEvents().add(event);
    }

    protected void onTransactionMessageReceived(TransactionDetails tx) {
        getBroadcastTransactionMessages().add(tx);
    }
}
