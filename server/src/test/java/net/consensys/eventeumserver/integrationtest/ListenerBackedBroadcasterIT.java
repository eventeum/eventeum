package net.consensys.eventeumserver.integrationtest;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.integration.broadcast.blockchain.ListenerInvokingBlockchainEventBroadcaster;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:application-test-db-listener.properties")
@Import(ListenerBackedBroadcasterIT.ListenerConfig.class)
public class ListenerBackedBroadcasterIT extends BaseIntegrationTest {

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

    @Override
    protected List<BlockDetails> getBroadcastBlockMessages() {
        return ListenerConfig.broadcastBlockMessages;
    }

    @Override
    protected List<ContractEventDetails> getBroadcastContractEvents() {
        return ListenerConfig.broadcastContractEvents;
    }

    @TestConfiguration
    static class ListenerConfig {

        private static List<BlockDetails> broadcastBlockMessages = new ArrayList<>();

        private static List<ContractEventDetails> broadcastContractEvents = new ArrayList<>();

        @Bean
        public BlockchainEventBroadcaster listenerBroadcaster() {

            return new ListenerInvokingBlockchainEventBroadcaster(new ListenerInvokingBlockchainEventBroadcaster.OnBlockchainEventListener() {
                @Override
                public void onNewBlock(BlockDetails block) {
                    broadcastBlockMessages.add(block);
                }

                @Override
                public void onContractEvent(ContractEventDetails eventDetails) {
                    broadcastContractEvents.add(eventDetails);
                }
            });
        }
    }
}
