//package net.consensys.eventeum.chain.contract;
//
//import net.consensys.eventeum.chain.service.EventBlockManagementService;
//import net.consensys.eventeum.ContractEventDetails;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.math.BigInteger;
//
//import static org.mockito.Mockito.*;
//
//public class EventBlockUpdaterListenerTest {
//
//    private static final String ADDRESS = "0x2250683dbe4e0b90395c3c5d7def87784a2b916c";
//
//    private EventBlockUpdaterListener underTest;
//
//    private EventBlockManagementService mockBlockManagementService;
//
//    @Before
//    public void init() {
//        mockBlockManagementService = mock(EventBlockManagementService.class);
//
//        underTest = new EventBlockUpdaterListener(mockBlockManagementService);
//    }
//
//    @Test
//    public void testOnEvent() {
//        final ContractEventDetails eventDetails = mock(ContractEventDetails.class);
//        when(eventDetails.getEventSpecificationSignature()).thenReturn("spec");
//        when(eventDetails.getBlockNumber()).thenReturn(BigInteger.TEN);
//        when(eventDetails.getAddress()).thenReturn(ADDRESS);
//
//        underTest.onEvent(eventDetails);
//
//        verify(mockBlockManagementService, times(1)).updateLatestBlock("spec", BigInteger.TEN, ADDRESS);
//    }
//}
