package net.consensys.eventeumserver.integrationtest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.factory.ContractEventFilterRepositoryFactory;
import net.consensys.eventeum.factory.EventStoreFactory;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.integration.broadcast.blockchain.ListenerInvokingBlockchainEventBroadcaster;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
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
                    public Page<ContractEventDetails> getContractEventsForSignature(String eventSignature, PageRequest pagination) {
                        return null;
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