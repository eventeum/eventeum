package net.consensys.eventeum.chain.block.tx;

import net.consensys.eventeum.dto.transaction.TransactionDetails;

public interface TransactionMatchingCriteria {

    boolean isAMatch(TransactionDetails tx);

    boolean isOneTimeMatch();
}
