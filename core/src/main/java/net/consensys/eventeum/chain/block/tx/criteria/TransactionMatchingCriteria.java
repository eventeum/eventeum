package net.consensys.eventeum.chain.block.tx.criteria;

import net.consensys.eventeum.dto.transaction.TransactionDetails;

import java.util.List;

public interface TransactionMatchingCriteria {

    String getNodeName();

    List<String> getStatuses();

    boolean isAMatch(TransactionDetails tx);

    boolean isOneTimeMatch();

    boolean canBeRemoved();
}
