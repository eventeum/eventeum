package net.consensys.eventeum.integration.eventstore.local;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
import net.consensys.eventeum.integration.eventstore.local.repository.ContractEventDetailsRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * A saveable event store that stores contract events in a local repository.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
@ConditionalOnProperty(name = "eventStore.type", havingValue = "LOCAL")
public class LocalRepositoryEventStore implements SaveableEventStore {

    private ContractEventDetailsRepository repository;

    public LocalRepositoryEventStore(ContractEventDetailsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<ContractEventDetails> getContractEventsForSignature(String eventSignature, PageRequest pagination) {
        return repository.findByEventSpecificationSignature(eventSignature, pagination);
    }

    @Override
    public void save(ContractEventDetails contractEventDetails) {
        repository.save(contractEventDetails);
    }
}
