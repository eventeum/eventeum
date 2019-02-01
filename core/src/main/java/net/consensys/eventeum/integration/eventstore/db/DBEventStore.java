package net.consensys.eventeum.integration.eventstore.db;

import java.util.List;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.factory.EventStoreFactory;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
import net.consensys.eventeum.integration.eventstore.db.repository.ContractEventDetailsRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/**
 * A saveable event store that stores contract events in a db repository.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class DBEventStore implements SaveableEventStore {

    private ContractEventDetailsRepository repository;

    private MongoTemplate mongoTemplate;

    public DBEventStore(ContractEventDetailsRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<ContractEventDetails> getContractEventsForSignature(String eventSignature, PageRequest pagination) {

        final Query query = new Query(Criteria.where("eventSpecificationSignature").is(eventSignature))
            .with(new Sort(Direction.DESC, "blockNumber"))
            .collation(Collation.of("en").numericOrderingEnabled());

        final long totalResults = mongoTemplate.count(query, ContractEventDetails.class);

        //Set pagination on query
        query
            .skip(pagination.getPageNumber() * pagination.getPageSize())
            .limit(pagination.getPageSize());

        final List<ContractEventDetails> results = mongoTemplate.find(query, ContractEventDetails.class);

        return new PageImpl<>(results, pagination, totalResults);
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
