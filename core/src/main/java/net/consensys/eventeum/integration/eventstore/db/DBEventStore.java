package net.consensys.eventeum.integration.eventstore.db;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.factory.EventStoreFactory;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
import net.consensys.eventeum.integration.eventstore.db.repository.ContractEventDetailsRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * A saveable event store that stores contract events in a db repository.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class DBEventStore implements SaveableEventStore {

    private ContractEventDetailsRepository repository;

    public DBEventStore(ContractEventDetailsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<ContractEventDetails> getContractEventsForSignature(String eventSignature, PageRequest pagination) {
        return repository.findByEventSpecificationSignature(eventSignature, pagination);
    }

    @Override
    public boolean isPagingZeroIndexed() {
        return true;
    }

    @Override
    public void save(ContractEventDetails contractEventDetails) {
        repository.save(contractEventDetails);
    }
}
