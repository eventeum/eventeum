package net.consensys.eventeumserver.integrationtest;

import net.consensys.eventeum.chain.util.Web3jUtil;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.dto.event.filter.ParameterDefinition;
import net.consensys.eventeum.dto.event.filter.ParameterType;
import net.consensys.eventeum.dto.event.parameter.ArrayParameter;
import net.consensys.eventeum.dto.event.parameter.EventParameter;
import net.consensys.eventeum.dto.event.parameter.NumberParameter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:application-test-db.properties")
public class ArraysIT extends BaseKafkaIntegrationTest {

    @Test
    public void testUint256Array() throws Exception {
        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter registeredFilter = registerDummyEventArrayFilter(emitter.getContractAddress());
        emitter.emitEventArray(BigInteger.ONE, BigInteger.TEN).send();

        waitForContractEventMessages(1);

        assertEquals(1, getBroadcastContractEvents().size());

        final ContractEventDetails eventDetails = getBroadcastContractEvents().get(0);

        assertEquals(registeredFilter.getEventSpecification().getEventName(), eventDetails.getName());
        assertEquals(ContractEventStatus.UNCONFIRMED, eventDetails.getStatus());

        final ArrayList<NumberParameter> eventArray =
                (ArrayList<NumberParameter>) eventDetails.getNonIndexedParameters().get(0).getValue();

        assertEquals(BigInteger.ONE, eventArray.get(0).getValue());
        assertEquals(BigInteger.TEN, eventArray.get(1).getValue());
        assertEquals(Web3jUtil.getSignature(registeredFilter.getEventSpecification()),
                eventDetails.getEventSpecificationSignature());
    }

    private ContractEventFilter registerDummyEventArrayFilter(String contractAddress) {
        return registerEventFilter(createDummyEventArrayFilter(contractAddress));
    }

    private ContractEventFilter createDummyEventArrayFilter(String contractAddress) {

        final ContractEventSpecification eventSpec = new ContractEventSpecification();

        eventSpec.setNonIndexedParameterDefinitions(
                Arrays.asList(new ParameterDefinition(0, ParameterType.UINT256_ARRAY)));

        eventSpec.setEventName("DummyEventArray");

        return createFilter(getDummyEventFilterId(), contractAddress, eventSpec);
    }
}
