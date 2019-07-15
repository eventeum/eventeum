package net.consensys.eventeum.chain.block.tx;

import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.model.TransactionMonitoringSpec;

public interface TransactionMonitoringBlockListener extends BlockListener {

    void addMatchingCriteria(TransactionMatchingCriteria matchingCriteria);

    void removeMatchingCriteria(TransactionMatchingCriteria matchingCriteria);
}
