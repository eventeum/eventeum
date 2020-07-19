/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.consensys.eventeum.service;

import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.contract.ContractEventListener;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.container.NodeServices;
import net.consensys.eventeum.chain.service.strategy.BlockSubscriptionStrategy;
import net.consensys.eventeum.constant.Constants;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.dto.event.filter.ParameterDefinition;
import net.consensys.eventeum.dto.event.filter.ParameterType;
import net.consensys.eventeum.integration.broadcast.internal.EventeumEventBroadcaster;
import net.consensys.eventeum.repository.ContractEventFilterRepository;
import net.consensys.eventeum.service.exception.NotFoundException;
import net.consensys.eventeum.service.sync.EventSyncService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.retry.support.RetryTemplate;

import java.util.Arrays;
import java.util.Collections;

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
    private ChainServicesContainer mockChainServicesContainer;
    @Mock
    private NodeServices mockNodeServices;
    @Mock
    private BlockchainService mockBlockchainService;
    @Mock
    private BlockSubscriptionStrategy mockBlockSubscriptionStrategy;
    @Mock
    private ContractEventFilterRepository mockRepo;
    @Mock
    private EventeumEventBroadcaster mockFilterBroadcaster;
    @Mock
    private BlockListener mockBlockListener1;
    @Mock
    private BlockListener mockBlockListener2;
    @Mock
    private ContractEventListener mockEventListener1;
    @Mock
    private ContractEventListener mockEventListener2;
    @Mock
    private EventSyncService mockEventSyncService;

    private RetryTemplate mockRetryTemplate;

    static {
        eventSpec = new ContractEventSpecification();
        eventSpec.setEventName(EVENT_NAME);

        eventSpec.setIndexedParameterDefinitions(Arrays.asList(
                new ParameterDefinition(0, ParameterType.build("UINT256"))));

        eventSpec.setNonIndexedParameterDefinitions(
                Arrays.asList(new ParameterDefinition(1, ParameterType.build("UINT256")),
                        new ParameterDefinition(2, ParameterType.build("ADDRESS"))));
    }

    @Before
    public void init() {
        when(mockChainServicesContainer.getNodeServices(
                Constants.DEFAULT_NODE_NAME)).thenReturn(mockNodeServices);
        when(mockChainServicesContainer.getNodeNames()).thenReturn(
                Collections.singletonList(Constants.DEFAULT_NODE_NAME));
        when(mockNodeServices.getBlockSubscriptionStrategy()).thenReturn(mockBlockSubscriptionStrategy);

        mockRetryTemplate = new RetryTemplate();

        underTest = new DefaultSubscriptionService(mockChainServicesContainer,
                mockRepo, mockFilterBroadcaster, Arrays.asList(mockBlockListener1, mockBlockListener2),
                mockRetryTemplate, mockEventSyncService);
    }

    @Test
    public void testSubscribeToNewBlocksOnInit() {

        underTest.init(null);

        verify(mockBlockSubscriptionStrategy, times(1)).addBlockListener(mockBlockListener1);
        verify(mockBlockSubscriptionStrategy, times(1)).addBlockListener(mockBlockListener2);
    }

    @Test
    public void testRegisterNewContractEventFilter() {
        final ContractEventFilter filter = createEventFilter();
        underTest.registerContractEventFilter(filter, true);

        verifyContractEventFilterBroadcast(filter,true);
        assertEquals(1, underTest.listContractEventFilters().size());
    }

    @Test
    public void testRegisterNewContractEventFilterBroadcastFalse() {
        final ContractEventFilter filter = createEventFilter();

        underTest.registerContractEventFilter(filter, false);

        assertEquals(1, underTest.listContractEventFilters().size());
    }

    @Test
    public void testRegisterNewContractEventFilterAlreadyRegistered() {
        final ContractEventFilter filter = createEventFilter();
        underTest.registerContractEventFilter(filter, true);
        underTest.registerContractEventFilter(filter, true);

        verifyContractEventFilterBroadcast(filter,true);

        assertEquals(1, underTest.listContractEventFilters().size());
    }

    @Test
    public void testRegisterNewContractEventFilterAutoGenerateId() {
        final ContractEventFilter filter = createEventFilter(null, Constants.DEFAULT_NODE_NAME);

        underTest.registerContractEventFilter(filter, true);

        assertTrue(!filter.getId().isEmpty());
        assertEquals(1, underTest.listContractEventFilters().size());
    }

    @Test
    public void testListContractEventFilterAlreadyRegistered() {
        final ContractEventFilter filter1 = createEventFilter(null, Constants.DEFAULT_NODE_NAME);

        underTest.registerContractEventFilter(filter1, true);
        underTest.registerContractEventFilter(filter1, true);

        assertEquals(1, underTest.listContractEventFilters().size());
    }

    @Test
    public void testUnregisterContractEventFilter() throws NotFoundException {
        final ContractEventFilter filter = createEventFilter();

        underTest.registerContractEventFilter(filter, false);

        underTest.unregisterContractEventFilter(FILTER_ID);

        verify(mockRepo, times(1)).deleteById(FILTER_ID);
        verify(mockFilterBroadcaster, times(1)).broadcastEventFilterRemoved(filter);
        assertEquals(0, underTest.listContractEventFilters().size());

        boolean exceptionThrown = false;
        //This will test that the filter has been deleted from memory
        try {
            underTest.unregisterContractEventFilter(FILTER_ID);
        } catch (NotFoundException e) {
            //Expected
            exceptionThrown = true;
        }

        assertEquals(true, exceptionThrown);
    }

    @Test
    public void testUnsubscribeToAllSubscriptions() {
        final ContractEventFilter filter1 = createEventFilter("filter1", Constants.DEFAULT_NODE_NAME);
        final ContractEventFilter filter2 = createEventFilter();

        underTest.registerContractEventFilter(filter1, false);
        underTest.registerContractEventFilter(filter2, false);
        underTest.unsubscribeToAllSubscriptions(Constants.DEFAULT_NODE_NAME);

        assertEquals(0, underTest.listContractEventFilters().size());
    }

    private void verifyContractEventFilterBroadcast(ContractEventFilter filter, boolean save) {
        int expectedSaveInvocations = save ? 1 : 0;
        verify(mockRepo, times(expectedSaveInvocations)).save(filter);

        verify(mockFilterBroadcaster, times(1)).broadcastEventFilterAdded(filter);
    }

    private ContractEventFilter createEventFilter(String id, String nodeName) {
        final ContractEventFilter filter = new ContractEventFilter();

        filter.setId(id);
        filter.setContractAddress(CONTRACT_ADDRESS);
        filter.setEventSpecification(eventSpec);
        filter.setNode(nodeName);

        return filter;
    }

    private ContractEventFilter createEventFilter() {
        final ContractEventFilter filter = createEventFilter(FILTER_ID, Constants.DEFAULT_NODE_NAME);

        return filter;
    }

}
