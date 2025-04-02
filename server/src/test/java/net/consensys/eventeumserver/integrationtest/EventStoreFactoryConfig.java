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

package net.consensys.eventeumserver.integrationtest;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.message.MessageDetails;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.factory.EventStoreFactory;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.integration.broadcast.blockchain.ListenerInvokingBlockchainEventBroadcaster;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
import net.consensys.eventeum.model.LatestBlock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@TestConfiguration
public class EventStoreFactoryConfig {

  @Bean
  public BlockchainEventBroadcaster listenerBroadcaster() {

    return new ListenerInvokingBlockchainEventBroadcaster(
        new ListenerInvokingBlockchainEventBroadcaster.OnBlockchainEventListener() {
          @Override
          public void onNewBlock(BlockDetails block) {
            // DO NOTHING
          }

          @Override
          public void onContractEvent(ContractEventDetails eventDetails) {
            // DO NOTHING
          }

          @Override
          public void onTransactionEvent(TransactionDetails transactionDetails) {
            // DO NOTHING
          }

          @Override
          public void onMessageEvent(MessageDetails messageDetails) {
            // DO NOTHING
          }
        });
  }

  @Bean
  public EventStoreFactory eventStoreFactory() {
    return new EventStoreFactory() {

      @Override
      public SaveableEventStore build() {
        return new SaveableEventStore() {
          @Override
          public void save(ContractEventDetails contractEventDetails) {
            savedEvents().getEntities().add(contractEventDetails);
          }

          @Override
          public void save(LatestBlock latestBlock) {
            savedLatestBlock().getEntities().clear();
            savedLatestBlock().getEntities().add(latestBlock);
          }

          @Override
          public void save(MessageDetails messageDetails) {
            savedMessages().getEntities().add(messageDetails);
          }

          @Override
          public Page<ContractEventDetails> getContractEventsForSignature(
              String eventSignature, String contractAddress, PageRequest pagination) {
            return null;
          }

          @Override
          public Optional<LatestBlock> getLatestBlockForNode(String nodeName) {
            return Optional.empty();
          }

          @Override
          public boolean isPagingZeroIndexed() {
            return false;
          }

          @Override
          public Optional<MessageDetails> getLatestMessageFromTopic(
              String nodeName, String topicId) {
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
        };
      }
    };
  }

  @Bean
  Entities<ContractEventDetails> savedEvents() {
    return new Entities<>();
  }

  @Bean
  Entities<LatestBlock> savedLatestBlock() {
    return new Entities<>();
  }

  @Bean
  Entities<MessageDetails> savedMessages() {
    return new Entities<>();
  }

  public class Entities<T> {
    List<T> entities = new ArrayList<>();

    public List<T> getEntities() {
      return entities;
    }
  }

  public class EventStoreSavedContractEvents {
    private List<ContractEventDetails> savedEvents = new ArrayList<>();

    public List<ContractEventDetails> getSavedEvents() {
      return savedEvents;
    }
  }
}
