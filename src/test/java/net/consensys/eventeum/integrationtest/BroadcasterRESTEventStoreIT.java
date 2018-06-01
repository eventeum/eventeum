package net.consensys.eventeum.integrationtest;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:application-test.properties")
public class BroadcasterRESTEventStoreIT extends MainBroadcasterTests {

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
