package io.keyko.monitoring.agent.core.chain.block.tx;

import io.keyko.monitoring.agent.core.chain.block.tx.criteria.TransactionMatchingCriteria;
import io.keyko.monitoring.agent.core.chain.block.BlockListener;

public interface TransactionMonitoringBlockListener extends BlockListener {

    void addMatchingCriteria(TransactionMatchingCriteria matchingCriteria);

    void removeMatchingCriteria(TransactionMatchingCriteria matchingCriteria);
}
