package io.keyko.monitoring.agent.core.chain.block.tx.criteria.factory;

import io.keyko.monitoring.agent.core.chain.block.tx.criteria.TransactionMatchingCriteria;
import io.keyko.monitoring.agent.core.model.TransactionMonitoringSpec;

public interface TransactionMatchingCriteriaFactory {

    TransactionMatchingCriteria build(TransactionMonitoringSpec spec);
}
