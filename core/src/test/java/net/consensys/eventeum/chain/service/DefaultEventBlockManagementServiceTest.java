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

package net.consensys.eventeum.chain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;
import net.consensys.eventeum.chain.service.block.DefaultEventBlockManagementService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.container.NodeServices;
import net.consensys.eventeum.chain.settings.Node;
import net.consensys.eventeum.chain.settings.NodeSettings;
import net.consensys.eventeum.constant.Constants;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.dto.event.filter.ParameterDefinition;
import net.consensys.eventeum.dto.event.filter.ParameterType;
import net.consensys.eventeum.service.EventStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultEventBlockManagementServiceTest {

  private static final String EVENT_SPEC_HASH =
      "0x4d44a3f7bbdc3ab16cf28ad5234f38b7464ff912da473754ab39f0f97692eded";

  private static final String CONTRACT_ADDRESS = "0xd94a9d6733a64cecdcc8ca01da72554b4d883a47";

  private static final ContractEventSpecification EVENT_SPEC;

  private static final ContractEventFilter EVENT_FILTER;

  private DefaultEventBlockManagementService underTest;

  private BlockchainService mockBlockchainService;

  private EventStoreService mockEventStoreService;

  private NodeServices mockNodeServices;

  private ChainServicesContainer mockChainServicesContainer;

  private NodeSettings mockNodeSettings;

  static {
    EVENT_SPEC = new ContractEventSpecification();
    EVENT_SPEC.setEventName("AnEvent");
    EVENT_SPEC.setIndexedParameterDefinitions(
        Arrays.asList(
            new ParameterDefinition(0, ParameterType.build("ADDRESS")),
            new ParameterDefinition(1, ParameterType.build("UINT256"))));

    EVENT_FILTER = new ContractEventFilter();
    EVENT_FILTER.setNode(Constants.DEFAULT_NODE_NAME);
    EVENT_FILTER.setEventSpecification(EVENT_SPEC);
    EVENT_FILTER.setContractAddress(CONTRACT_ADDRESS);

    EVENT_SPEC.setNonIndexedParameterDefinitions(
        Arrays.asList(new ParameterDefinition(2, ParameterType.build("BYTES32"))));
  }

  @BeforeEach
  public void init() {
    mockNodeServices = mock(NodeServices.class);
    mockChainServicesContainer = mock(ChainServicesContainer.class);
    mockBlockchainService = mock(BlockchainService.class);
    mockEventStoreService = mock(EventStoreService.class);
    mockNodeSettings = mock(NodeSettings.class);

    when(mockChainServicesContainer.getNodeServices(Constants.DEFAULT_NODE_NAME))
        .thenReturn(mockNodeServices);
    when(mockNodeServices.getBlockchainService()).thenReturn(mockBlockchainService);
    when(mockBlockchainService.getCurrentBlockNumber()).thenReturn(BigInteger.valueOf(1000));

    Node node = new Node();

    node.setInitialStartBlock(
        BigInteger.valueOf(Long.valueOf(NodeSettings.DEFAULT_SYNC_START_BLOCK)));
    node.setMaxBlocksToSync(BigInteger.valueOf(7200));

    when(mockNodeSettings.getNode(Constants.DEFAULT_NODE_NAME)).thenReturn(node);

    underTest =
        new DefaultEventBlockManagementService(
            mockChainServicesContainer, mockEventStoreService, mockNodeSettings);
  }

  @Test
  public void testUpdateAndGetNoMatch() {
    underTest.updateLatestBlock(EVENT_SPEC_HASH, BigInteger.TEN, CONTRACT_ADDRESS);
    final BigInteger result = underTest.getLatestBlockForEvent(EVENT_FILTER);

    assertEquals(BigInteger.TEN.add(BigInteger.ONE), result);
  }

  @Test
  public void testUpdateAndGetLowerMatch() {
    underTest.updateLatestBlock(EVENT_SPEC_HASH, BigInteger.ONE, CONTRACT_ADDRESS);
    underTest.updateLatestBlock(EVENT_SPEC_HASH, BigInteger.TEN, CONTRACT_ADDRESS);
    final BigInteger result = underTest.getLatestBlockForEvent(EVENT_FILTER);

    assertEquals(BigInteger.TEN.add(BigInteger.ONE), result);
  }

  @Test
  public void testUpdateAndGetHigherMatch() {
    underTest.updateLatestBlock(EVENT_SPEC_HASH, BigInteger.TEN, CONTRACT_ADDRESS);
    underTest.updateLatestBlock(EVENT_SPEC_HASH, BigInteger.ONE, CONTRACT_ADDRESS);
    final BigInteger result = underTest.getLatestBlockForEvent(EVENT_FILTER);

    assertEquals(BigInteger.TEN.add(BigInteger.ONE), result);
  }

  @Test
  public void testGetNoLocalMatchButHitInEventStore() {
    final ContractEventDetails mockEventDetails = mock(ContractEventDetails.class);
    when(mockEventDetails.getBlockNumber()).thenReturn(BigInteger.ONE);
    when(mockEventStoreService.getLatestContractEvent(EVENT_SPEC_HASH, CONTRACT_ADDRESS))
        .thenReturn(Optional.of(mockEventDetails));

    final BigInteger result = underTest.getLatestBlockForEvent(EVENT_FILTER);

    assertEquals(BigInteger.ONE.add(BigInteger.ONE), result);
  }

  @Test
  public void testGetNoLocalMatchAndNoHitInEventStore() {
    when(mockEventStoreService.getLatestContractEvent(EVENT_SPEC_HASH, CONTRACT_ADDRESS))
        .thenReturn(null);
    when(mockBlockchainService.getCurrentBlockNumber()).thenReturn(BigInteger.valueOf(20));
    when(mockEventStoreService.getLatestContractEvent(EVENT_SPEC_HASH, CONTRACT_ADDRESS))
        .thenReturn(Optional.empty());

    final BigInteger result = underTest.getLatestBlockForEvent(EVENT_FILTER);

    assertEquals(BigInteger.valueOf(20), result);
  }
}
