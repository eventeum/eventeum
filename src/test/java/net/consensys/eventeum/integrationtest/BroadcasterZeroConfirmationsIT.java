package net.consensys.eventeum.integrationtest;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.Message;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations={"classpath:application-test.properties", "classpath:zero-confirmations.properties"})
public class BroadcasterZeroConfirmationsIT extends BaseKafkaIntegrationTest {

    @Test
    public void testBroadcastsUnconfirmedAndConfirmedEventAfterInitialEmit() throws Exception {

        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter registeredFilter = registerDummyEventFilter(emitter.getContractAddress());
        emitter.emit(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();

        waitForContractEventMessages(1);

        assertEquals(1, getBroadcastContractEvents().size());

        final ContractEventDetails eventDetails = getBroadcastContractEvents().get(0);
        verifyDummyEventDetails(registeredFilter, eventDetails, ContractEventStatus.CONFIRMED);
    }

    private ContractEventFilter doRegisterAndUnregister(String contractAddress) {
        final ContractEventFilter registeredFilter = registerDummyEventFilter(contractAddress
        );

        ContractEventFilter saved = getFilterRepo().findOne(getDummyEventFilterId());
        assertEquals(registeredFilter, saved);

        unregisterDummyEventFilter();

        saved = getFilterRepo().findOne(getDummyEventFilterId());
        assertNull(saved);

        return registeredFilter;
    }
}
