package net.consensys.eventeum.chain.contract;

import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.config.EventConfirmationConfig;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.mockito.Mockito.*;

public class ConfirmationCheckInitialiserTest {

     private ConfirmationCheckInitialiser underTest;

     private BlockchainService mockBlockchainService;
     private BlockListener mockBlockListener;

     @Before
     public void init() {
         mockBlockchainService = mock(BlockchainService.class);
         mockBlockListener = mock(BlockListener.class);

         final EventConfirmationConfig config = new EventConfirmationConfig(BigInteger.TEN, BigInteger.valueOf(100));

         underTest = new ConfirmationCheckInitialiserForTest(mockBlockchainService,
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

         return eventDetails;
     }

     private class ConfirmationCheckInitialiserForTest extends ConfirmationCheckInitialiser {

         public ConfirmationCheckInitialiserForTest(BlockchainService blockchainService,
                                                    BlockchainEventBroadcaster eventBroadcaster,
                                                    EventConfirmationConfig eventConfirmationConfig) {
             super(blockchainService, eventBroadcaster, eventConfirmationConfig);
         }

         @Override
         protected BlockListener createEventConfirmationBlockListener(ContractEventDetails eventDetails) {
             return mockBlockListener;
         }
     }
}
