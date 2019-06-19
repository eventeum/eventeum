package net.consensys.eventeumserver.integrationtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:application-test-http.properties")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HttpBroadcasterIT extends BaseIntegrationTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testBroadcastBlock() throws Exception {
        StubHttpConsumer consumer = new StubHttpConsumer();
        consumer.start(getBroadcastContractEvents(), getBroadcastBlockMessages());

        triggerBlocks(1);
        waitForBlockMessages(1);

        consumer.stop();

        BlockDetails blockDetails = getBroadcastBlockMessages().get(0);
        assertEquals(1, blockDetails.getNumber().compareTo(BigInteger.ZERO));
        assertNotNull(blockDetails.getHash());
        assertNotNull(blockDetails.getTimestamp());
    }

    @Test
    public void testBroadcastContractEvent() throws Exception {
        StubHttpConsumer consumer = new StubHttpConsumer();
        consumer.start(getBroadcastContractEvents(), getBroadcastBlockMessages());

        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter registeredFilter = registerDummyEventFilter(emitter.getContractAddress());
        emitter.emit(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();

        waitForContractEventMessages(1);

        consumer.stop();

        assertEquals(1, getBroadcastContractEvents().size());

        final ContractEventDetails eventDetails = getBroadcastContractEvents().get(0);
        verifyDummyEventDetails(registeredFilter, eventDetails, ContractEventStatus.UNCONFIRMED);
    }
}
