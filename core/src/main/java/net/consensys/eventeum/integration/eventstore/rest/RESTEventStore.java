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

package net.consensys.eventeum.integration.eventstore.rest;

import java.math.BigInteger;
import java.util.Optional;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.message.MessageDetails;
import net.consensys.eventeum.integration.eventstore.EventStore;
import net.consensys.eventeum.integration.eventstore.rest.client.EventStoreClient;
import net.consensys.eventeum.model.LatestBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * An event store implementation that integrates with an external REST api in order to obtain the
 * event details.
 *
 * <p>The REST events tore path can be specified with the eventStore.url and eventStore.eventPath
 * parameters.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class RESTEventStore implements EventStore {

  private EventStoreClient client;

  public RESTEventStore(EventStoreClient client) {
    this.client = client;
  }

  @Override
  public Page<ContractEventDetails> getContractEventsForSignature(
      String eventSignature, String contractAddress, PageRequest pagination) {

    final Sort.Order firstOrder = pagination.getSort().iterator().next();
    return client.getContractEvents(
        pagination.getPageNumber(),
        pagination.getPageSize(),
        firstOrder.getProperty(),
        firstOrder.getDirection(),
        eventSignature,
        contractAddress);
  }

  @Override
  public Optional<LatestBlock> getLatestBlockForNode(String nodeName) {
    return Optional.ofNullable(client.getLatestBlock(nodeName));
  }

  @Override
  public boolean isPagingZeroIndexed() {
    return true;
  }

  @Override
  public Optional<MessageDetails> getLatestMessageFromTopic(String nodeName, String topicId) {
    return Optional.empty();
  }

  @Override
  public Optional<ContractEventDetails> getContractEvent(
      String eventSignature,
      String contractAddress,
      String blockHash,
      String transactionHash,
      BigInteger logIndex) {
    return Optional.empty();
  }
}
