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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.parameter.NumberParameter;
import net.consensys.eventeum.dto.event.parameter.StringParameter;
import net.consensys.eventeum.dto.message.MessageDetails;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.integration.broadcast.blockchain.HttpBlockchainEventBroadcaster;
import net.consensys.eventeum.integration.broadcast.blockchain.HttpBroadcasterSettings;
import net.consensys.eventeum.testutils.StubHttpConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

public class HttpBlockchainEventBroadcasterTest {

  private HttpBlockchainEventBroadcaster underTest;

  private StubHttpConsumer httpConsumer;

  @BeforeEach
  public void init() {
    final HttpBroadcasterSettings settings = new HttpBroadcasterSettings();
    settings.setBlockEventsUrl("http://localhost:8082/consumer/block-event");
    settings.setContractEventsUrl("http://localhost:8082/consumer/contract-event");
    settings.setTransactionEventsUrl("http://localhost:8082/consumer/transaction-event");
    settings.setMessageEventsUrl("http://localhost:8082/consumer/message-event");

    RetryTemplate retryTemplate = new RetryTemplate();

    FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
    fixedBackOffPolicy.setBackOffPeriod(3000l);
    retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

    SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
    retryPolicy.setMaxAttempts(3);
    retryTemplate.setRetryPolicy(retryPolicy);

    underTest = new HttpBlockchainEventBroadcaster(settings, retryTemplate);
  }

  @AfterEach
  public void cleanup() {
    httpConsumer.stop();
  }

  @Test
  public void testBroadcastContractEvent() {
    final List<ContractEventDetails> broadcastEvents = new ArrayList<>();
    httpConsumer = new StubHttpConsumer(HttpStatus.OK);

    final ContractEventDetails contractEvent = createContractEventDetails();

    httpConsumer.start(broadcastEvents);
    underTest.broadcastContractEvent(contractEvent);

    assertEquals(1, broadcastEvents.size());
    assertEquals(contractEvent, broadcastEvents.get(0));
  }

  @Test
  public void testBroadcastBlockEvent() throws IOException {
    httpConsumer = new StubHttpConsumer(HttpStatus.OK);

    final BlockDetails block = new BlockDetails();
    block.setHash("0xc2141b870536473fdea321893bc084eb3244cc56ea8d4b77de240dfeac6604d2");
    block.setNumber(BigInteger.TEN);

    httpConsumer.start(null);
    underTest.broadcastNewBlock(block);

    ObjectMapper mapper = new ObjectMapper();
    BlockDetails broadcastBlock =
        mapper.readValue(httpConsumer.getLatestRequestBody(), BlockDetails.class);

    assertEquals(block, broadcastBlock);
  }

  @Test
  public void testBroadcasterTransactionEvent() throws IOException {
    httpConsumer = new StubHttpConsumer(HttpStatus.OK);

    final TransactionDetails transaction = new TransactionDetails();
    transaction.setHash("0xc2141b870536473fdea321893bc084eb3244cc56ea8d4b77de240dfeac6604d2");

    httpConsumer.start(null);
    underTest.broadcastTransaction(transaction);

    ObjectMapper mapper = new ObjectMapper();
    TransactionDetails transactionDetails =
        mapper.readValue(httpConsumer.getLatestRequestBody(), TransactionDetails.class);

    assertEquals(transaction, transactionDetails);
  }

  @Test
  public void testBroadcasterMessageEvent() throws IOException {
    httpConsumer = new StubHttpConsumer(HttpStatus.OK);

    final MessageDetails message = new MessageDetails();
    message.setMessage("Hello world!");
    message.setTopicId("0.0.1");

    httpConsumer.start(null);
    underTest.broadcastMessage(message);

    ObjectMapper mapper = new ObjectMapper();
    MessageDetails messageDetails =
        mapper.readValue(httpConsumer.getLatestRequestBody(), MessageDetails.class);

    assertEquals(message, messageDetails);
  }

  private ContractEventDetails createContractEventDetails() {
    final ContractEventDetails contractEvent = new ContractEventDetails();
    contractEvent.setBlockNumber(BigInteger.TEN);
    contractEvent.setStatus(ContractEventStatus.CONFIRMED);
    contractEvent.setFilterId(UUID.randomUUID().toString());
    contractEvent.setBlockHash(
        "0xc2141b870536473fdea321893bc084eb3244cc56ea8d4b77de240dfeac6604d2");
    contractEvent.setLogIndex(BigInteger.ONE);
    contractEvent.setTransactionHash(
        "0x4744d9c8c368be18d010832bf19cc5f35fe0d3f5f800fec20f9f1ca10a1820f7");
    contractEvent.setName("AnEvent");
    contractEvent.setAddress("0xf0a6c84894ed7312a75ff0e621cde2f8a1c62d6f");
    contractEvent.setEventSpecificationSignature("somesig");
    contractEvent.setIndexedParameters(
        Arrays.asList(
            new StringParameter("bytes32", "1234"),
            new NumberParameter("uint256", BigInteger.valueOf(123))));
    contractEvent.setNonIndexedParameters(
        Arrays.asList(
            new StringParameter("string", "5678"),
            new NumberParameter("uint256", BigInteger.valueOf(456))));

    return contractEvent;
  }
}
