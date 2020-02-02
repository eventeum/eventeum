package io.keyko.monitoring.agent.core.repository;

import io.keyko.monitoring.agent.core.factory.ContractEventFilterRepositoryFactory;
import io.keyko.monitoring.agent.core.model.TransactionMonitoringSpec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring repository for storing active TransactionMonitoringSpec(s) in DB.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Repository
@ConditionalOnMissingBean(ContractEventFilterRepositoryFactory.class)
public interface TransactionMonitoringSpecRepository extends CrudRepository<TransactionMonitoringSpec, String> {
}
