package net.consensys.eventeum.repository;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring repository for storing active ContractEventFilters in mongoDB.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Repository
public interface ContractEventFilterRepository extends MongoRepository<ContractEventFilter, String> {
}