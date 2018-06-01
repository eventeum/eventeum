package net.consensys.eventeum.service;

import junit.framework.TestCase;
import net.consensys.eventeum.chain.contract.ContractEventListener;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.dto.event.filter.ParameterDefinition;
import net.consensys.eventeum.dto.event.filter.ParameterType;
import net.consensys.eventeum.integration.broadcast.FilterEventBroadcaster;
import net.consensys.eventeum.repository.ContractEventFilterRepository;
import net.consensys.eventeum.testutils.DummyAsyncTaskService;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.service.BlockchainService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import rx.Subscription;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultSubscriptionServiceTest {
    private static final String FILTER_ID = "123-456";

    private static final String EVENT_NAME = "DummyEvent";

    private static final String CONTRACT_ADDRESS = "0x7a55a28856d43bba3c6a7e36f2cee9a82923e99b";

    private static ContractEventSpecification eventSpec;

    private DefaultSubscriptionService underTest;

    @Mock
    private BlockchainService mockBlockchainService;
    @Mock
    private ContractEventFilterRepository mockRepo;
    @Mock
    private FilterEventBroadcaster mockFilterBroadcaster;
    @Mock
    private BlockListener mockBlockListener1;
    @Mock
    private BlockListener mockBlockListener2;
    @Mock
    private ContractEventListener mockEventListener1;
    @Mock
    private ContractEventListener mockEventListener2;

    static {
        eventSpec = new ContractEventSpecification();
        eventSpec.setEventName(EVENT_NAME);

        eventSpec.setIndexedParameterDefinitions(Arrays.asList(new ParameterDefinition(0, ParameterType.UINT256)));

        eventSpec.setNonIndexedParameterDefinitions(
                Arrays.asList(new ParameterDefinition(1, ParameterType.UINT256),
                        new ParameterDefinition(2, ParameterType.ADDRESS)));
    }

    @Before
    public void init() {
        underTest = new DefaultSubscriptionService(mockBlockchainService,
                mockRepo, mockFilterBroadcaster, new DummyAsyncTaskService(),
                Arrays.asList(mockBlockListener1, mockBlockListener2),
                Arrays.asList(mockEventListener1, mockEventListener2));
    }

    @Test
    public void testSubscribeToNewBlocksOnConstruction() {
        verify(mockBlockchainService, times(1)).addBlockListener(mockBlockListener1);
        verify(mockBlockchainService, times(1)).addBlockListener(mockBlockListener2);
    }

    @Test
    public void testRegisterNewContractEventFilter() {
        final ContractEventFilter filter = createEventFilter();
        underTest.registerContractEventFilter(filter);

        verifyContractEventFilterRegistration(filter,true, true);
    }

    @Test
    public void testRegisterNewContractEventFilterBroadcastFalse() {
        final ContractEventFilter filter = createEventFilter();
        underTest.registerContractEventFilter(filter, false);

        verifyContractEventFilterRegistration(filter,true, false);
    }

    @Test
    public void testRegisterNewContractEventFilterAlreadyRegistered() {
        final ContractEventFilter filter = createEventFilter();
        underTest.registerContractEventFilter(filter, true);
        underTest.registerContractEventFilter(filter, true);

        verifyContractEventFilterRegistration(filter,true, true);
    }

    @Test
    public void testRegisterNewContractEventFilterAutoGenerateId() {
        final ContractEventFilter filter = createEventFilter(null);
        underTest.registerContractEventFilter(filter, true);

        assertTrue(!filter.getId().isEmpty());
    }

    @Test
    public void testResubscribeToAllSubscriptionsUnsubscribeFirst() {
        final ContractEventFilter filter1 = createEventFilter(FILTER_ID);
        final ContractEventFilter filter2 = createEventFilter("AnotherId");
        final Subscription sub1 = mock(Subscription.class);
        final Subscription sub2 = mock(Subscription.class);

        when(mockBlockchainService.registerEventListener(
                eq(filter1), any(ContractEventListener.class))).thenReturn(sub1);
        when(mockBlockchainService.registerEventListener(
                eq(filter2), any(ContractEventListener.class))).thenReturn(sub2);

        //Add 2 filters
        underTest.registerContractEventFilter(filter1, true);
        underTest.registerContractEventFilter(filter2, true);

        reset(mockBlockchainService, mockRepo, mockFilterBroadcaster);

        underTest.resubscribeToAllSubscriptions(true);

        verify(sub1, times(1)).unsubscribe();
        verify(sub2, times(1)).unsubscribe();

        verifyContractEventFilterRegistration(filter1, false, false);
        verifyContractEventFilterRegistration(filter2, false, false);
    }

    @Test
    public void testUnnregisterContractEventFilter() throws FilterNotFoundException {
        final ContractEventFilter filter = createEventFilter();
        final Subscription sub1 = mock(Subscription.class);

        when(mockBlockchainService.registerEventListener(
                eq(filter), any(ContractEventListener.class))).thenReturn(sub1);

        underTest.registerContractEventFilter(filter, false);

        underTest.unregisterContractEventFilter(FILTER_ID);

        verify(sub1, times(1)).unsubscribe();
        verify(mockRepo, times(1)).delete(FILTER_ID);
        verify(mockFilterBroadcaster, times(1)).broadcastEventFilterRemoved(filter);

        boolean exceptionThrown = false;
        //This will test that the filter has been deleted from memory
        try {
            underTest.unregisterContractEventFilter(FILTER_ID);
        } catch (FilterNotFoundException e) {
            //Expected
            exceptionThrown = true;
        }

        assertEquals(true, exceptionThrown);
    }

    private void verifyContractEventFilterRegistration(ContractEventFilter filter, boolean save, boolean broadcast) {
        verify(mockBlockchainService, times(1)).registerEventListener(eq(filter), any(ContractEventListener.class));

        int expectedSaveInvocations = save ? 1 : 0;
        verify(mockRepo, times(expectedSaveInvocations)).save(filter);

        int expectedBroadcastInvocations = broadcast ? 1 : 0;
        verify(mockFilterBroadcaster, times(expectedBroadcastInvocations)).broadcastEventFilterAdded(filter);
    }

    private ContractEventFilter createEventFilter(String id) {
        final ContractEventFilter filter = new ContractEventFilter();

        filter.setId(id);
        filter.setContractAddress(CONTRACT_ADDRESS);
        filter.setEventSpecification(eventSpec);

        return filter;
    }

    private ContractEventFilter createEventFilter() {
        return createEventFilter(FILTER_ID);
    }

}
