package net.consensys.eventeum.chain.block.tx.criteria.factory;

import net.consensys.eventeum.chain.block.tx.criteria.TransactionMatchingCriteria;
import net.consensys.eventeum.model.TransactionMonitoringSpec;

public interface TransactionMatchingCriteriaFactory {

    TransactionMatchingCriteria build(TransactionMonitoringSpec spec);
}
