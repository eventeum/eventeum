package net.consensys.eventeum.chain.service.factory;

import net.consensys.eventeum.chain.config.EventConfirmationConfig;
import net.consensys.eventeum.chain.converter.EventParameterConverter;
import net.consensys.eventeum.chain.util.Web3jUtil;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.dto.event.filter.ParameterDefinition;
import net.consensys.eventeum.dto.event.filter.ParameterType;
import net.consensys.eventeum.dto.event.parameter.EventParameter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.web3j.abi.datatypes.Type;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultContractEventDetailsFactoryTest {

    //Values: 123, 0x00a329c0648769a73afac7f9381e08fb43dbea72
    private static final String LOG_DATA = "0x000000000000000000000000000000000000000000000000000000000000007b00000000000000000000000000a329c0648769a73afac7f9381e08fb43dbea72";

    //Values: 456
    private static final String INDEXED_PARAM = "0x00000000000000000000000000000000000000000000000000000000000001c8";

    private static final String CONTRACT_ADDRESS = "0x7a55a28856d43bba3c6a7e36f2cee9a82923e99b";

    private static final String EVENT_NAME = "DummyEvent";

    private static final String ADDRESS = "0x2250683dbe4e0b90395c3c5d7def87784a2b916c";

    private static final BigInteger LOG_INDEX = BigInteger.TEN;

    private static final String TX_HASH = "0x1fb4a22baf926bd643d796e1332b73452b4eeb1dc6e8be787d4bf54dcccf4485";

    private static final BigInteger BLOCK_NUMBER = BigInteger.valueOf(12345);

    private static final String BLOCK_HASH = "0xf6c7c0822df1bce82b8edf55ab93f2e69ea80ef714801789fae3b3a08f761047";

    private DefaultContactEventDetailsFactory underTest;

    private EventParameterConverter mockParameterCoverter;

    private org.web3j.protocol.core.methods.response.Log mockLog;

    private static ContractEventSpecification eventSpec;

    private ContractEventFilter filter;

    static {
        eventSpec = new ContractEventSpecification();
        eventSpec.setEventName(EVENT_NAME);
        eventSpec.setIndexedParameterDefinitions(
                Arrays.asList(new ParameterDefinition(0, ParameterType.UINT256)));

        eventSpec.setNonIndexedParameterDefinitions(Arrays.asList(
                new ParameterDefinition(1, ParameterType.UINT256),
                new ParameterDefinition(2, ParameterType.ADDRESS)));
    }

    @Before
    public void init() {
        mockParameterCoverter = mock(EventParameterConverter.class);

        mockLog = mock(org.web3j.protocol.core.methods.response.Log.class);
        when(mockLog.getData()).thenReturn(LOG_DATA);
        when(mockLog.getTopics()).thenReturn(Arrays.asList(null, INDEXED_PARAM));
        when(mockLog.getAddress()).thenReturn(ADDRESS);
        when(mockLog.getLogIndex()).thenReturn(LOG_INDEX);
        when(mockLog.getTransactionHash()).thenReturn(TX_HASH);
        when(mockLog.getBlockNumber()).thenReturn(BLOCK_NUMBER);
        when(mockLog.getBlockHash()).thenReturn(BLOCK_HASH);

        filter = new ContractEventFilter();
        filter.setContractAddress(CONTRACT_ADDRESS);
        filter.setEventSpecification(eventSpec);
    }

    @Test
    public void testValuesCorrect() {
        DefaultContactEventDetailsFactory underTest = createFactory(BigInteger.TEN);

        final ContractEventDetails eventDetails = underTest.createEventDetails(filter, mockLog);

        assertEquals(eventDetails.getName(), eventSpec.getEventName());
        assertEquals(filter.getId(), eventDetails.getFilterId());
        assertEquals(ADDRESS, eventDetails.getAddress());
        assertEquals(LOG_INDEX, eventDetails.getLogIndex());
        assertEquals(TX_HASH, eventDetails.getTransactionHash());
        assertEquals(BLOCK_NUMBER, eventDetails.getBlockNumber());
        assertEquals(BLOCK_HASH, eventDetails.getBlockHash());
        assertEquals(Web3jUtil.getSignature(eventSpec), eventDetails.getEventSpecificationSignature());
        assertEquals(ContractEventStatus.UNCONFIRMED, eventDetails.getStatus());
    }

    @Test
    public void testStatusWhenLogRemoved() {
        when(mockLog.isRemoved()).thenReturn(true);

        DefaultContactEventDetailsFactory underTest = createFactory(BigInteger.TEN);

        final ContractEventDetails eventDetails = underTest.createEventDetails(filter, mockLog);

        assertEquals(ContractEventStatus.INVALIDATED, eventDetails.getStatus());
    }

    @Test
    public void testStatusWhenZeroConfirmationsConfigured() {
        DefaultContactEventDetailsFactory underTest = createFactory(BigInteger.ZERO);

        final ContractEventDetails eventDetails = underTest.createEventDetails(filter, mockLog);

        assertEquals(ContractEventStatus.CONFIRMED, eventDetails.getStatus());
    }

    @Test
    public void testIndexedParametersAreCorrect() {
        final DefaultContactEventDetailsFactory underTest = createFactory(BigInteger.TEN);

        final EventParameter mockParam1 = mock(EventParameter.class);
        final ArgumentCaptor<Type> argumentCaptor = ArgumentCaptor.forClass(Type.class);
        when(mockParameterCoverter.convert(argumentCaptor.capture())).thenReturn(mockParam1);

        final ContractEventDetails eventDetails = underTest.createEventDetails(filter, mockLog);

        assertEquals(Arrays.asList(mockParam1), eventDetails.getIndexedParameters());
        assertEquals(BigInteger.valueOf(456), argumentCaptor.getAllValues().get(2).getValue());
    }

    @Test
    public void testNonIndexedParametersAreCorrect() {

        final DefaultContactEventDetailsFactory underTest = createFactory(BigInteger.TEN);

        final EventParameter mockParam1 = mock(EventParameter.class);
        final ArgumentCaptor<Type> argumentCaptor = ArgumentCaptor.forClass(Type.class);
        when(mockParameterCoverter.convert(argumentCaptor.capture())).thenReturn(mockParam1);

        final ContractEventDetails eventDetails = underTest.createEventDetails(filter, mockLog);

        assertEquals(Arrays.asList(mockParam1, mockParam1), eventDetails.getNonIndexedParameters());
        assertEquals(BigInteger.valueOf(123), argumentCaptor.getAllValues().get(0).getValue());
        assertEquals("0x00a329c0648769a73afac7f9381e08fb43dbea72",
                argumentCaptor.getAllValues().get(1).toString());
    }

    private DefaultContactEventDetailsFactory createFactory(BigInteger confirmations) {
        final EventConfirmationConfig config = new EventConfirmationConfig(confirmations, BigInteger.valueOf(100));
        return new DefaultContactEventDetailsFactory(mockParameterCoverter, config);
    }
}
