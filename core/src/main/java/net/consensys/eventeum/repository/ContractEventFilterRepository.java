package net.consensys.eventeum.repository;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.factory.ContractEventFilterRepositoryFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring repository for storing active ContractEventFilters in mongoDB.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Repository
@ConditionalOnMissingBean(ContractEventFilterRepositoryFactory.class)
public interface ContractEventFilterRepository extends MongoRepository<ContractEventFilter, String> {
}