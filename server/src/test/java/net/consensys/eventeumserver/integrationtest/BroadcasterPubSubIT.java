package net.consensys.eventeumserver.integrationtest;

import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.domain.TransactionReceipt;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:application-test-ws-pubsub.properties")
public class BroadcasterPubSubIT extends MainBroadcasterTests {


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
}
