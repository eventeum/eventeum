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

package net.consensys.eventeum.integration.broadcast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.math.BigInteger;
import java.util.Optional;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.event.filter.correlationId.ParameterCorrelationIdStrategy;
import net.consensys.eventeum.dto.message.BlockEvent;
import net.consensys.eventeum.dto.message.ContractEvent;
import net.consensys.eventeum.dto.message.EventeumMessage;
import net.consensys.eventeum.dto.message.MessageDetails;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.integration.KafkaSettings;
import net.consensys.eventeum.integration.broadcast.blockchain.KafkaBlockchainEventBroadcaster;
import net.consensys.eventeum.repository.ContractEventFilterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaBlockchainEventBroadcasterTest {

  private static final String BLOCK_EVENTS_TOPIC = "ThisIsABlockTopic";

  private static final String CONTRACT_EVENTS_TOPIC = "ThisIsAnEventTopic";

  private static final String TRANSACTION_EVENTS_TOPIC = "ThisIsAnTransactionTopic";

  private static final String MESSAGE_EVENTS_TOPIC = "ThisIsAnMessageTopic";

  private static final String FILTER_ID = "filter-id";

  private KafkaBlockchainEventBroadcaster underTest;

  private KafkaTemplate<String, EventeumMessage> mockKafkaTemplate;

  private KafkaSettings mockKafkaSettings;

  private ContractEventFilterRepository mockFilterRepository;

  @BeforeEach
  public void init() {
    mockKafkaTemplate = mock(KafkaTemplate.class);
    mockKafkaSettings = mock(KafkaSettings.class);
    mockFilterRepository = mock(ContractEventFilterRepository.class);

    when(mockKafkaSettings.getBlockEventsTopic()).thenReturn(BLOCK_EVENTS_TOPIC);
    when(mockKafkaSettings.getContractEventsTopic()).thenReturn(CONTRACT_EVENTS_TOPIC);
    when(mockKafkaSettings.getTransactionEventsTopic()).thenReturn(TRANSACTION_EVENTS_TOPIC);
    when(mockKafkaSettings.getMessageEventsTopic()).thenReturn(MESSAGE_EVENTS_TOPIC);

    underTest =
        new KafkaBlockchainEventBroadcaster(
            mockKafkaTemplate, mockKafkaSettings, mockFilterRepository);
  }

  @Test
  public void testBroadcastNewBlock() {
    final BlockDetails blockDetails = createBlockDetails();

    underTest.broadcastNewBlock(blockDetails);

    final ArgumentCaptor<EventeumMessage> eventCaptor =
        ArgumentCaptor.forClass(EventeumMessage.class);
    final ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockKafkaTemplate)
        .send(eq(BLOCK_EVENTS_TOPIC), idCaptor.capture(), eventCaptor.capture());

    assertEquals(BlockEvent.TYPE, eventCaptor.getValue().getType());
    assertEquals(blockDetails, eventCaptor.getValue().getDetails());
    assertNotNull(idCaptor.getValue());
    assertEquals(eventCaptor.getValue().getId(), idCaptor.getValue());
  }

  @Test
  public void testBroadcastContractEvent() {
    final ContractEventDetails eventDetails = createContractEventDetails();

    underTest.broadcastContractEvent(eventDetails);

    final ArgumentCaptor<EventeumMessage> eventCaptor =
        ArgumentCaptor.forClass(EventeumMessage.class);
    verify(mockKafkaTemplate).send(eq(CONTRACT_EVENTS_TOPIC), anyString(), eventCaptor.capture());

    assertEquals(ContractEvent.TYPE, eventCaptor.getValue().getType());
    assertEquals(eventDetails, eventCaptor.getValue().getDetails());
  }

  @Test
  public void testBroadcastContractEventDefaultCorrelationId() {
    final ContractEventDetails eventDetails = createContractEventDetails();

    underTest.broadcastContractEvent(eventDetails);

    final ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockKafkaTemplate)
        .send(eq(CONTRACT_EVENTS_TOPIC), idCaptor.capture(), any(EventeumMessage.class));

    assertEquals(eventDetails.getEventIdentifier(), idCaptor.getValue());
  }

  @Test
  public void testCorrelationIdWhenStrategySetOnFilter() {

    final ContractEventDetails eventDetails = createContractEventDetails();

    final ParameterCorrelationIdStrategy mockIdStrategy =
        mock(ParameterCorrelationIdStrategy.class);
    when(mockIdStrategy.getCorrelationId(eventDetails)).thenReturn("12-34");

    final ContractEventFilter mockFilter = mock(ContractEventFilter.class);
    when(mockFilter.getCorrelationIdStrategy()).thenReturn(mockIdStrategy);

    when(mockFilterRepository.findById(FILTER_ID)).thenReturn(Optional.of(mockFilter));

    underTest.broadcastContractEvent(eventDetails);

    final ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockKafkaTemplate)
        .send(eq(CONTRACT_EVENTS_TOPIC), idCaptor.capture(), any(EventeumMessage.class));

    assertNotNull(idCaptor.getValue());
    assertEquals("12-34", idCaptor.getValue());
  }

  @Test
  public void testBroadcastTransactionEvent() {
    final TransactionDetails transactionDetails = createTransactionDetails();

    underTest.broadcastTransaction(transactionDetails);

    final ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockKafkaTemplate)
        .send(eq(TRANSACTION_EVENTS_TOPIC), idCaptor.capture(), any(EventeumMessage.class));

    assertEquals(transactionDetails.getBlockHash(), idCaptor.getValue());
  }

  @Test
  public void testBroadcastMessageEvent() {
    final MessageDetails messageDetails = createMessageDetails();

    underTest.broadcastMessage(messageDetails);

    final ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockKafkaTemplate)
        .send(eq(MESSAGE_EVENTS_TOPIC), idCaptor.capture(), any(EventeumMessage.class));

    assertEquals(messageDetails.getTopicId(), idCaptor.getValue());
  }

  private BlockDetails createBlockDetails() {
    final BlockDetails blockDetails = new BlockDetails();
    blockDetails.setHash("0x86e01e667d3e9a0c624ca2e30b1b452973b7ba2802bb2f2c30ce399dd6131741");

    return blockDetails;
  }

  private ContractEventDetails createContractEventDetails() {
    final ContractEventDetails contractEventDetails = new ContractEventDetails();
    contractEventDetails.setBlockHash(
        "0x86e01e667d3e9a0c624ca2e30b1b452973b7ba2802bb2f2c30ce399dd6131741");
    contractEventDetails.setTransactionHash(
        "0x7ba0d5bf4dd88d9bca44957460a7e69fffbf9604288a7d4e4a9d6c7e75c627b4");
    contractEventDetails.setLogIndex(BigInteger.ONE);
    contractEventDetails.setFilterId(FILTER_ID);

    return contractEventDetails;
  }

  private TransactionDetails createTransactionDetails() {
    final TransactionDetails transactionDetails = new TransactionDetails();
    transactionDetails.setBlockHash(
        "0x86e01e667d3e9a0c624ca2e30b1b452973b7ba2802bb2f2c30ce399dd6131741");
    transactionDetails.setHash(
        "0x7ba0d5bf4dd88d9bca44957460a7e69fffbf9604288a7d4e4a9d6c7e75c627b4");

    return transactionDetails;
  }

  private MessageDetails createMessageDetails() {
    final MessageDetails messageDetails = new MessageDetails();
    messageDetails.setMessage("Hello world!");
    messageDetails.setTopicId("0.0.1");

    return messageDetails;
  }
}
