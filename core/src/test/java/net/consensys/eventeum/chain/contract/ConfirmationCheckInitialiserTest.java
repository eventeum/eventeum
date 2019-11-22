package net.consensys.eventeum.chain.contract;

import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.container.NodeServices;
import net.consensys.eventeum.chain.settings.Node;
import net.consensys.eventeum.chain.settings.NodeSettings;
import net.consensys.eventeum.constant.Constants;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
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
     private NodeSettings mockNodeSettings;
     private AsyncTaskService asyncTaskService = new DummyAsyncTaskService();
     private BigInteger currentBlock = BigInteger.valueOf(2000);

     @Before
     public void init() {

         mockBlockchainService = mock(BlockchainService.class);
         mockBlockListener = mock(BlockListener.class);
         mockChainServicesContainer = mock(ChainServicesContainer.class);
         mockNodeServices = mock(NodeServices.class);
         mockNodeSettings = mock(NodeSettings.class);

         when(mockChainServicesContainer.getNodeServices(Constants.DEFAULT_NODE_NAME))
                 .thenReturn(mockNodeServices);

         when(mockNodeServices.getBlockchainService()).thenReturn(mockBlockchainService);
         when(mockBlockchainService.getCurrentBlockNumber()).thenReturn(currentBlock);
         Node node =
                 new Node();
         node.setBlocksToWaitForConfirmation(BigInteger.valueOf(10));
         node.setBlocksToWaitForMissingTx(BigInteger.valueOf(100));
         node.setBlocksToWaitBeforeInvalidating(BigInteger.valueOf(5));
         when(mockNodeSettings.getNode(any())).thenReturn(node);

         underTest = new ConfirmationCheckInitialiserForTest(mockChainServicesContainer,
                 mock(BlockchainEventBroadcaster.class), mockNodeSettings);
     }

     @Test
         public void testOnEventNotInvalidated() {
            ContractEventDetails event = createContractEventDetails(ContractEventStatus.UNCONFIRMED);
             when(event.getBlockNumber()).thenReturn(currentBlock);
             underTest.onEvent(event);

             verify(mockBlockchainService, times(1)).addBlockListener(mockBlockListener);
         }

    @Test
    public void testOnEventInvalidated() {
        underTest.onEvent(createContractEventDetails(ContractEventStatus.INVALIDATED));

        verify(mockBlockchainService, never()).addBlockListener(mockBlockListener);
    }

    @Test
    public void testOnEventWithAExpiredBlockEvent() {
        ContractEventDetails event = createContractEventDetails(ContractEventStatus.UNCONFIRMED);
        when(event.getBlockNumber()).thenReturn(BigInteger.valueOf(1));
        underTest.onEvent(event);

        verify(mockBlockchainService, times(0)).addBlockListener(mockBlockListener);
    }

     private ContractEventDetails createContractEventDetails(ContractEventStatus status) {
         final ContractEventDetails eventDetails = mock(ContractEventDetails.class);

         when(eventDetails.getStatus()).thenReturn(status);
         when(eventDetails.getNodeName()).thenReturn(Constants.DEFAULT_NODE_NAME);
         when(eventDetails.getBlockNumber()).thenReturn(BigInteger.valueOf(0));

         return eventDetails;
     }

     private class ConfirmationCheckInitialiserForTest extends ConfirmationCheckInitialiser {

         public ConfirmationCheckInitialiserForTest(ChainServicesContainer chainServicesContainer,
                                                    BlockchainEventBroadcaster eventBroadcaster,
                                                    NodeSettings node) {
             super(chainServicesContainer, eventBroadcaster, asyncTaskService,node);
         }

         @Override
         protected BlockListener createEventConfirmationBlockListener(ContractEventDetails eventDetails,Node node) {
             return mockBlockListener;
         }
     }
}
