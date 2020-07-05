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

import net.consensys.eventeum.constant.Constants;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.*;
import net.consensys.eventeum.model.TransactionIdentifierType;
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

import java.util.List;
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
    public void testListEventFilters() {
        final ContractEventFilter filter = createDummyEventFilter(FAKE_CONTRACT_ADDRESS);

        registerEventFilter(filter);

	List<ContractEventFilter> contractEventFilters = listEventFilters();

	assertEquals(1, contractEventFilters.size());
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
    public void testUnregisterEventFilterDeletesFilterInDb() throws InterruptedException {
        final ContractEventFilter registeredFilter = registerDummyEventFilter(FAKE_CONTRACT_ADDRESS);

        Optional<ContractEventFilter> saved = getFilterRepo().findById(getDummyEventFilterId());
        assertEquals(registeredFilter, saved.get());

        Thread.sleep(1000);
        unregisterDummyEventFilter();
        Thread.sleep(1000);

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
        TransactionMonitoringSpec monitorSpec = new TransactionMonitoringSpec(TransactionIdentifierType.HASH, txHash, Constants.DEFAULT_NODE_NAME);

        final String monitorId = monitorTransaction(monitorSpec);

        transactionMonitoringSpecRepository.findAll();
        final Optional<TransactionMonitoringSpec> saved =
                transactionMonitoringSpecRepository.findById(monitorId);
        assertEquals(monitorId, saved.get().getId());
        assertEquals(txHash, saved.get().getTransactionIdentifierValue());

        return monitorId;
    }

    @Test
    public void testRegisterTransactionMonitorBroadcastsAddedMessage() throws InterruptedException {
        final String txHash = generateTxHash();

        TransactionMonitoringSpec monitorSpec = new TransactionMonitoringSpec(TransactionIdentifierType.HASH, txHash, Constants.DEFAULT_NODE_NAME);

        monitorTransaction(monitorSpec);
        waitForBroadcast();
        assertEquals(1, getBroadcastTransactionEventMessages().size());

        final EventeumMessage<TransactionMonitoringSpec> broadcastMessage =
                getBroadcastTransactionEventMessages().get(0);

        assertEquals(true, broadcastMessage instanceof TransactionMonitorAdded);
        assertEquals(txHash, broadcastMessage.getDetails().getTransactionIdentifierValue());
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
        assertEquals(txHash, broadcastMessage.getDetails().getTransactionIdentifierValue());
    }

    private String generateTxHash() {
        return Hash.sha3String(UUID.randomUUID().toString());
    }
}
