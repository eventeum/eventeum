package net.consensys.eventeum.integrationtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test-http.properties")
public class HttpBroadcasterIT extends BaseIntegrationTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testBroadcastBlock() throws Exception {
        StubHttpConsumer consumer = new StubHttpConsumer();
        consumer.start(getBroadcastContractEvents());

        triggerBlocks(1);
        Thread.sleep(15000);

        consumer.stop();

        BlockDetails blockDetails = mapper.readValue(consumer.getLatestRequestBody(), BlockDetails.class);
        assertEquals(1, blockDetails.getNumber().compareTo(BigInteger.ZERO));
        assertNotNull(blockDetails.getHash());
    }

    @Test
    public void testBroadcastContractEvent() throws Exception {
        StubHttpConsumer consumer = new StubHttpConsumer();
        consumer.start(getBroadcastContractEvents());

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
