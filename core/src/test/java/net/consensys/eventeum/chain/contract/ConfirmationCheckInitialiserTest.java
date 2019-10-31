package net.consensys.eventeum.chain.contract;

import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.config.EventConfirmationConfig;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.container.NodeServices;
import net.consensys.eventeum.constant.Constants;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.service.AsyncTaskService;
import net.consensys.eventeum.testutils.DummyAsyncTaskService;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.mockito.Mockito.*;

public class ConfirmationCheckInitialiserTest {

     private ConfirmationCheckInitialiser underTest;

     private BlockchainService mockBlockchainService;
     private BlockListener mockBlockListener;
     private ChainServicesContainer mockChainServicesContainer;
     private NodeServices mockNodeServices;
     private AsyncTaskService asyncTaskService = new DummyAsyncTaskService();

     @Before
     public void init() {
         mockBlockchainService = mock(BlockchainService.class);
         mockBlockListener = mock(BlockListener.class);
         mockChainServicesContainer = mock(ChainServicesContainer.class);
         mockNodeServices = mock(NodeServices.class);

         when(mockChainServicesContainer.getNodeServices(Constants.DEFAULT_NODE_NAME))
                 .thenReturn(mockNodeServices);

         when(mockNodeServices.getBlockchainService()).thenReturn(mockBlockchainService);

         final EventConfirmationConfig config = new EventConfirmationConfig(BigInteger.TEN, BigInteger.valueOf(100), BigInteger.valueOf(5));

         underTest = new ConfirmationCheckInitialiserForTest(mockChainServicesContainer,
                 mock(BlockchainEventBroadcaster.class), config);
     }

     @Test
     public void testOnEventNotInvalidated() {
         underTest.onEvent(createContractEventDetails(ContractEventStatus.UNCONFIRMED));

         verify(mockBlockchainService, times(1)).addBlockListener(mockBlockListener);
     }

    @Test
    public void testOnEventInvalidated() {
        underTest.onEvent(createContractEventDetails(ContractEventStatus.INVALIDATED));

        verify(mockBlockchainService, never()).addBlockListener(mockBlockListener);
    }

     private ContractEventDetails createContractEventDetails(ContractEventStatus status) {
         final ContractEventDetails eventDetails = mock(ContractEventDetails.class);

         when(eventDetails.getStatus()).thenReturn(status);
         when(eventDetails.getNodeName()).thenReturn(Constants.DEFAULT_NODE_NAME);

         return eventDetails;
     }

     private class ConfirmationCheckInitialiserForTest extends ConfirmationCheckInitialiser {

         public ConfirmationCheckInitialiserForTest(ChainServicesContainer chainServicesContainer,
                                                    BlockchainEventBroadcaster eventBroadcaster,
                                                    EventConfirmationConfig eventConfirmationConfig) {
             super(chainServicesContainer, eventBroadcaster, eventConfirmationConfig, asyncTaskService);
         }

         @Override
         protected BlockListener createEventConfirmationBlockListener(ContractEventDetails eventDetails) {
             return mockBlockListener;
         }
     }
}
