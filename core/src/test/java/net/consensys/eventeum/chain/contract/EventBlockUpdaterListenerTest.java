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

package net.consensys.eventeum.chain.contract;

import net.consensys.eventeum.chain.service.block.EventBlockManagementService;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.mockito.Mockito.*;

public class EventBlockUpdaterListenerTest {

    private static final String ADDRESS = "0x2250683dbe4e0b90395c3c5d7def87784a2b916c";

    private EventBlockUpdaterListener underTest;

    private EventBlockManagementService mockBlockManagementService;

    @Before
    public void init() {
        mockBlockManagementService = mock(EventBlockManagementService.class);

        underTest = new EventBlockUpdaterListener(mockBlockManagementService);
    }

    @Test
    public void testOnEvent() {
        final ContractEventDetails eventDetails = mock(ContractEventDetails.class);
        when(eventDetails.getEventSpecificationSignature()).thenReturn("spec");
        when(eventDetails.getBlockNumber()).thenReturn(BigInteger.TEN);
        when(eventDetails.getAddress()).thenReturn(ADDRESS);

        underTest.onEvent(eventDetails);

        verify(mockBlockManagementService, times(1)).updateLatestBlock("spec", BigInteger.TEN, ADDRESS);
    }
}
