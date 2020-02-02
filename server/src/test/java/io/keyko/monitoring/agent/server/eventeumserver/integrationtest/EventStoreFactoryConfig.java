package io.keyko.monitoring.agent.server.eventeumserver.integrationtest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.keyko.monitoring.agent.core.dto.block.BlockDetails;
import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionDetails;
import io.keyko.monitoring.agent.core.factory.ContractEventFilterRepositoryFactory;
import io.keyko.monitoring.agent.core.factory.EventStoreFactory;
import io.keyko.monitoring.agent.core.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import io.keyko.monitoring.agent.core.integration.broadcast.blockchain.ListenerInvokingBlockchainEventBroadcaster;
import io.keyko.monitoring.agent.core.integration.eventstore.SaveableEventStore;
import io.keyko.monitoring.agent.core.model.LatestBlock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;

@TestConfiguration
public class EventStoreFactoryConfig {

    @Bean
    public BlockchainEventBroadcaster listenerBroadcaster() {

        return new ListenerInvokingBlockchainEventBroadcaster(new ListenerInvokingBlockchainEventBroadcaster.OnBlockchainEventListener() {
            @Override
            public void onNewBlock(BlockDetails block) {
                //DO NOTHING
            }

            @Override
            public void onContractEvent(ContractEventDetails eventDetails) {
                //DO NOTHING
            }

            @Override
            public void onTransactionEvent(TransactionDetails transactionDetails) {
                //DO NOTHING
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