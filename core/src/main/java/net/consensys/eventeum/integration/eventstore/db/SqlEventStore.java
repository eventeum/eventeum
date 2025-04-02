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

package net.consensys.eventeum.integration.eventstore.db;

import java.math.BigInteger;
import java.util.Optional;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.message.MessageDetails;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
import net.consensys.eventeum.integration.eventstore.db.repository.ContractEventDetailsRepository;
import net.consensys.eventeum.integration.eventstore.db.repository.LatestBlockRepository;
import net.consensys.eventeum.integration.eventstore.db.repository.MessageDetailsRepository;
import net.consensys.eventeum.model.LatestBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * A saveable event store that stores contract events in a db repository.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class SqlEventStore implements SaveableEventStore {

  private final ContractEventDetailsRepository eventDetailsRepository;

  private final MessageDetailsRepository messageDetailsRepository;

  private final LatestBlockRepository latestBlockRepository;

  public SqlEventStore(
      ContractEventDetailsRepository eventDetailsRepository,
      MessageDetailsRepository messageDetailsRepository,
      LatestBlockRepository latestBlockRepository) {
    this.messageDetailsRepository = messageDetailsRepository;
    this.eventDetailsRepository = eventDetailsRepository;
    this.latestBlockRepository = latestBlockRepository;
  }

  @Override
  public Page<ContractEventDetails> getContractEventsForSignature(
      String eventSignature, String contractAddress, PageRequest pagination) {
    return eventDetailsRepository.findByEventSpecificationSignatureAndAddress(
        eventSignature, contractAddress, pagination);
  }

  @Override
  public Optional<LatestBlock> getLatestBlockForNode(String nodeName) {
    final Iterable<LatestBlock> blocks = latestBlockRepository.findAll();

    return latestBlockRepository.findById(nodeName);
  }

  @Override
  public boolean isPagingZeroIndexed() {
    return true;
  }

  @Override
  public Optional<MessageDetails> getLatestMessageFromTopic(String nodeName, String topicId) {
    Sort.TypedSort<MessageDetails> message = Sort.sort(MessageDetails.class);
    return messageDetailsRepository.findFirstByNodeNameAndTopicId(
        nodeName, topicId, message.by(MessageDetails::getTimestamp).descending());
  }

  @Override
  public Optional<ContractEventDetails> getContractEvent(
      String eventSignature,
      String contractAddress,
      String blockHash,
      String transactionHash,
      BigInteger logIndex) {
    return eventDetailsRepository
        .findByEventSpecificationSignatureAndAddressAndBlockHashAndTransactionHashAndLogIndex(
            eventSignature, contractAddress, blockHash, transactionHash, logIndex);
  }

  @Override
  public void save(ContractEventDetails contractEventDetails) {
    eventDetailsRepository.save(contractEventDetails);
  }

  @Override
  public void save(LatestBlock latestBlock) {
    latestBlockRepository.save(latestBlock);
  }

  @Override
  public void save(MessageDetails messageDetails) {
    messageDetailsRepository.save(messageDetails);
  }
}
