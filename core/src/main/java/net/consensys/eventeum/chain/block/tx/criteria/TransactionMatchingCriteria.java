package net.consensys.eventeum.chain.block.tx.criteria;

import net.consensys.eventeum.dto.transaction.TransactionDetails;

public interface TransactionMatchingCriteria {

    String getNodeName();

    boolean isAMatch(TransactionDetails tx);

    boolean isOneTimeMatch();
}
