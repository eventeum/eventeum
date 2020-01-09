package net.consensys.eventeum.chain.block.tx.criteria;

import net.consensys.eventeum.TransactionDetails;
import net.consensys.eventeum.TransactionStatus;

import java.util.List;

public interface TransactionMatchingCriteria {

    String getNodeName();

    List<TransactionStatus> getStatuses();

    boolean isAMatch(TransactionDetails tx);

    boolean isOneTimeMatch();
}
