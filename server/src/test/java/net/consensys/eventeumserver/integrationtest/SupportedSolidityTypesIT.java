package net.consensys.eventeumserver.integrationtest;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.dto.event.filter.ParameterDefinition;
import net.consensys.eventeum.dto.event.filter.ParameterType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.utils.Numeric;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:application-test-db.properties")
public class SupportedSolidityTypesIT extends BaseKafkaIntegrationTest {


    @Test
    public void testBytes16Broadcast() throws Exception {
        final EventEmitter eventEmitter = deployEventEmitterContract();

        final ContractEventSpecification eventSpec = new ContractEventSpecification();
        eventSpec.setIndexedParameterDefinitions(
                Arrays.asList(new ParameterDefinition(0, ParameterType.build("BYTES16"))));

        eventSpec.setNonIndexedParameterDefinitions(
                Arrays.asList(new ParameterDefinition(1, ParameterType.build("BYTES16"))));

        eventSpec.setEventName(eventEmitter.DUMMYEVENTBYTES16_EVENT.getName());

        registerEventFilter(createFilter(null , eventEmitter.getContractAddress(), eventSpec));

        //Generate random 16 byte value
        final Random random = new Random();
        byte[] rndBytes = new byte[16];
        random.nextBytes(rndBytes);

        eventEmitter.emitEventBytes16(rndBytes).send();

        waitForContractEventMessages(1);

        final ContractEventDetails event = getBroadcastContractEvents().get(0);
        final String valueHex = Numeric.toHexString(rndBytes, 0, 16, true);

        assertEquals(valueHex, event.getNonIndexedParameters().get(0).getValueString());
        assertEquals(valueHex, event.getIndexedParameters().get(0).getValueString());
    }
}
