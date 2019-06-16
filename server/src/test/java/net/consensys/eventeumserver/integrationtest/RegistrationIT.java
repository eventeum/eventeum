package net.consensys.eventeumserver.integrationtest;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.*;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import net.consensys.eventeum.repository.TransactionMonitoringSpecRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.web3j.crypto.Hash;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:application-test-multiinstance.properties")
public class RegistrationIT extends BaseKafkaIntegrationTest {

    @Autowired
    private TransactionMonitoringSpecRepository transactionMonitoringSpecRepository;

    //Contract Event Filters
    @Test
    public void testRegisterEventFilterSavesFilterInDb() {
        final ContractEventFilter registeredFilter = registerDummyEventFilter(FAKE_CONTRACT_ADDRESS);

        final Optional<ContractEventFilter> saved = getFilterRepo().findById(getDummyEventFilterId());
        assertEquals(registeredFilter, saved.get());
    }

    @Test
    public void testRegisterEventFilterBroadcastsAddedMessage() throws InterruptedException {
        final ContractEventFilter registeredFilter = registerDummyEventFilter(FAKE_CONTRACT_ADDRESS);

        waitForBroadcast();
        assertEquals(1, getBroadcastFilterEventMessages().size());

        final EventeumMessage<ContractEventFilter> broadcastMessage = getBroadcastFilterEventMessages().get(0);

        assertEquals(true, broadcastMessage instanceof ContractEventFilterAdded);
        assertEquals(registeredFilter, broadcastMessage.getDetails());
    }

    @Test
    public void testRegisterEventFilterReturnsCreatedIdWhenNotSet() {
        final ContractEventFilter filter = createDummyEventFilter(FAKE_CONTRACT_ADDRESS);
        filter.setId(null);

        final ContractEventFilter registeredFilter = registerEventFilter(filter);
        assertNotNull(registeredFilter.getId());

        //This errors if id is not a valid UUID
        UUID.fromString(registeredFilter.getId());
    }

    @Test
    public void testRegisterEventFilterReturnsCorrectId() {
        final ContractEventFilter registeredFilter = registerDummyEventFilter(FAKE_CONTRACT_ADDRESS);

        assertEquals(getDummyEventFilterId(), registeredFilter.getId());
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

        Optional<ContractEventFilter> saved = getFilterRepo().findById(getDummyEventFilterId());
        assertEquals(registeredFilter, saved.get());

        unregisterDummyEventFilter();

        saved = getFilterRepo().findById(getDummyEventFilterId());
        assertFalse(saved.isPresent());
    }

    @Test
    public void testUnregisterEventFilterBroadcastsRemovedMessage() throws InterruptedException {
        final ContractEventFilter registeredFilter = doRegisterAndUnregister(FAKE_CONTRACT_ADDRESS);

        waitForBroadcast();
        assertEquals(2, getBroadcastFilterEventMessages().size());

        final EventeumMessage<ContractEventFilter> broadcastMessage = getBroadcastFilterEventMessages().get(1);

        assertEquals(true, broadcastMessage instanceof ContractEventFilterRemoved);
        assertEquals(registeredFilter, broadcastMessage.getDetails());
    }

    //Transaction Monitoring

    @Test
    public void testRegisterTransactionMonitorSavesInDb() {
        doTestRegisterTransactionMonitorSavesInDb(generateTxHash());
    }

    private String doTestRegisterTransactionMonitorSavesInDb(String txHash) {
        final String monitorId = monitorTransaction(txHash);

        transactionMonitoringSpecRepository.findAll();
        final Optional<TransactionMonitoringSpec> saved =
                transactionMonitoringSpecRepository.findById(monitorId);
        assertEquals(monitorId, saved.get().getId());
        assertEquals(txHash, saved.get().getTransactionIdentifier());

        return monitorId;
    }

    @Test
    public void testRegisterTransactionMonitorBroadcastsAddedMessage() throws InterruptedException {
        final String txHash = generateTxHash();

        monitorTransaction(txHash);
        waitForBroadcast();
        assertEquals(1, getBroadcastTransactionEventMessages().size());

        final EventeumMessage<TransactionMonitoringSpec> broadcastMessage =
                getBroadcastTransactionEventMessages().get(0);

        assertEquals(true, broadcastMessage instanceof TransactionMonitorAdded);
        assertEquals(txHash, broadcastMessage.getDetails().getTransactionIdentifier());
    }

    @Test
    public void testUnregisterNonExistentTransactionMonitor() {
        try {
            unregisterTransactionMonitor("NonExistent");
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
        }
    }

    @Test
    public void testUnregisterTransactionMonitorDeletesInDb() {
        final String monitorId = doTestRegisterTransactionMonitorSavesInDb(generateTxHash());

        unregisterTransactionMonitor(monitorId);

        assertFalse(transactionMonitoringSpecRepository.existsById(monitorId));
    }

    @Test
    public void testUnregisterTransactionMonitorBroadcastsRemovedMessage() throws InterruptedException {
        final String txHash = generateTxHash();
        String monitorId = doTestRegisterTransactionMonitorSavesInDb(txHash);

        unregisterTransactionMonitor(monitorId);

        waitForMessages(2, getBroadcastTransactionEventMessages());

        waitForBroadcast();
        assertEquals(2, getBroadcastTransactionEventMessages().size());

        final EventeumMessage<TransactionMonitoringSpec> broadcastMessage =
                getBroadcastTransactionEventMessages().get(1);

        assertEquals(true, broadcastMessage instanceof TransactionMonitorRemoved);
        assertEquals(monitorId, broadcastMessage.getDetails().getId());
        assertEquals(txHash, broadcastMessage.getDetails().getTransactionIdentifier());
    }

    private String generateTxHash() {
        return Hash.sha3String(UUID.randomUUID().toString());
    }
}
