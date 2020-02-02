package io.keyko.monitoring.agent.server.eventeumserver.integrationtest;

import io.keyko.monitoring.agent.core.chain.util.Web3jUtil;
import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.dto.event.ContractEventStatus;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventSpecification;
import io.keyko.monitoring.agent.core.dto.event.filter.ParameterDefinition;
import io.keyko.monitoring.agent.core.dto.event.filter.ParameterType;
import io.keyko.monitoring.agent.core.dto.event.parameter.NumberParameter;
import io.keyko.monitoring.agent.core.dto.event.parameter.StringParameter;
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

    //"BytesValue" in hex
    private static final String BYTES_VALUE_HEX = "0x427974657356616c756500000000000000000000000000000000000000000000";

    //"BytesValue2" in hex
    private static final String BYTES_VALUE2_HEX = "0x427974657356616c756532000000000000000000000000000000000000000000";

    @Test
    public void testEventWithArrays() throws Exception {
        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter registeredFilter = registerDummyEventArrayFilter(emitter.getContractAddress());
        emitter.emitEventArray(BigInteger.ONE, BigInteger.TEN,
                stringToBytes("BytesValue"), stringToBytes("BytesValue2")).send();

        waitForContractEventMessages(1);

        assertEquals(1, getBroadcastContractEvents().size());

        final ContractEventDetails eventDetails = getBroadcastContractEvents().get(0);

        assertEquals(registeredFilter.getEventSpecification().getEventName(), eventDetails.getName());
        assertEquals(ContractEventStatus.UNCONFIRMED, eventDetails.getStatus());

        final ArrayList<NumberParameter> uintArray =
                (ArrayList<NumberParameter>) eventDetails.getNonIndexedParameters().get(0).getValue();

        assertEquals(BigInteger.ONE, uintArray.get(0).getValue());
        assertEquals(BigInteger.TEN, uintArray.get(1).getValue());

        final ArrayList<StringParameter> bytesArray =
                (ArrayList<StringParameter>) eventDetails.getNonIndexedParameters().get(1).getValue();

        assertEquals(BYTES_VALUE_HEX, bytesArray.get(0).getValue());
        assertEquals(BYTES_VALUE2_HEX, bytesArray.get(1).getValue());

        assertEquals(Web3jUtil.getSignature(registeredFilter.getEventSpecification()),
                eventDetails.getEventSpecificationSignature());
    }

    private ContractEventFilter registerDummyEventArrayFilter(String contractAddress) {
        return registerEventFilter(createDummyEventArrayFilter(contractAddress));
    }

    private ContractEventFilter createDummyEventArrayFilter(String contractAddress) {

        final ContractEventSpecification eventSpec = new ContractEventSpecification();

        eventSpec.setNonIndexedParameterDefinitions(
                Arrays.asList(
                        new ParameterDefinition(0, ParameterType.build("UINT256[]"),""),
                        new ParameterDefinition(1, ParameterType.build("BYTES32[]"),"")));

        eventSpec.setEventName("DummyEventArray");

        return createFilter(getDummyEventFilterId(), contractAddress, eventSpec);
    }
}
