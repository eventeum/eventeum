package net.consensys.eventeum.repository;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.factory.ContractEventFilterRepositoryFactory;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring repository for storing active TransactionMonitoringSpec(s) in mongoDB.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Repository
@ConditionalOnMissingBean(ContractEventFilterRepositoryFactory.class)
public interface TransactionMonitoringSpecRepository extends MongoRepository<TransactionMonitoringSpec, String> {
}