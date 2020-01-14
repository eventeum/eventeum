package net.consensys.eventeumserver.integrationtest;

import static org.junit.Assert.assertEquals;


import java.math.BigInteger;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:application-test-factory.properties")
@Import(EventStoreFactoryConfig.class)
public class EventStoreFactoryIT extends BaseIntegrationTest {

    @Autowired
    private EventStoreFactoryConfig.Entities<ContractEventDetails> savedEvents;

    @Test
    public void testEventStoreFactoryWiredCorrectly() throws Exception {
        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter registeredFilter = registerDummyEventFilter(emitter.getContractAddress());
        emitter.emitEvent(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();

        Thread.sleep(15000);
        assertEquals(1, savedEvents.getEntities().size());

        final ContractEventDetails eventDetails = savedEvents.getEntities().get(0);
        verifyDummyEventDetails(registeredFilter, eventDetails, ContractEventStatus.CONFIRMED);
    }
}
