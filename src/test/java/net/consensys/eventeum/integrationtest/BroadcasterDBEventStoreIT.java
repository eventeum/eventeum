package net.consensys.eventeum.integrationtest;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import org.junit.Ignore;
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
@TestPropertySource(locations="classpath:application-test-db.properties")
public class BroadcasterDBEventStoreIT extends MainBroadcasterTests {

    @Ignore
    @Test
    public void testRegisterEventFilterSavesFilterInDb() {
        doTestRegisterEventFilterSavesFilterInDb();
    }
    @Ignore
    @Test
    public void testRegisterEventFilterBroadcastsAddedMessage() throws InterruptedException {
        doTestRegisterEventFilterBroadcastsAddedMessage();
    }
    @Ignore
    @Test
    public void testRegisterEventFilterReturnsCorrectId() {
        doTestRegisterEventFilterReturnsCorrectId();
    }
    @Ignore
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
    @Ignore
    @Test
    public void testBroadcastsConfirmedEventAfterBlockThresholdReached() throws Exception {
        doTestBroadcastsConfirmedEventAfterBlockThresholdReached();
    }
    @Ignore
    @Test
    public void testUnregisterNonExistentFilter() {
        doTestUnregisterNonExistentFilter();
    }
    @Ignore
    @Test
    public void testUnregisterEventFilterDeletesFilterInDb() {
        doTestUnregisterEventFilterDeletesFilterInDb();
    }
    @Ignore
    @Test
    public void testUnregisterEventFilterBroadcastsRemovedMessage() throws InterruptedException {
        doTestUnregisterEventFilterBroadcastsRemovedMessage();
    }
    @Ignore
    @Test
    public void testContractEventForUnregisteredEventFilterNotBroadcast() throws Exception {
        doTestContractEventForUnregisteredEventFilterNotBroadcast();
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
