package net.consensys.eventeumserver.integrationtest;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.EventeumMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:application-test-db-rabbit.properties")
public class RabbitBroadcasterDBEventStoreIT extends BaseRabbitIntegrationTest {

    public static FixedHostPortGenericContainer rabbitContainer;

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
        emitter.emit(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();

        waitForContractEventMessages(1);

        assertEquals(1, getBroadcastContractEvents().size());

        final ContractEventDetails eventDetails = getBroadcastContractEvents().get(0);
        verifyDummyEventDetails(registeredFilter, eventDetails, ContractEventStatus.CONFIRMED);
    }

    @TestConfiguration
    static class RabbitConfig {

        private List<EventeumMessage<BlockDetails>> broadcastBlockMessages = new ArrayList<>();
        private List<EventeumMessage<ContractEventDetails>> broadcastEventMessages = new ArrayList<>();

        @PostConstruct
        void initRabbit() {
            if (RabbitBroadcasterDBEventStoreIT.rabbitContainer == null) {
                RabbitBroadcasterDBEventStoreIT.rabbitContainer = new FixedHostPortGenericContainer("rabbitmq:3.6.14-management");
                RabbitBroadcasterDBEventStoreIT.rabbitContainer.waitingFor(Wait.forListeningPort());
                RabbitBroadcasterDBEventStoreIT.rabbitContainer.withFixedExposedPort(5672, 5672);
                RabbitBroadcasterDBEventStoreIT.rabbitContainer.start();
            }

        }
    }
}
