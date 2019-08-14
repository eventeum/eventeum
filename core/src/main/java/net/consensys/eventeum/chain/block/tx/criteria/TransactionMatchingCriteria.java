package net.consensys.eventeum.chain.block.tx.criteria;

import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.dto.transaction.TransactionStatus;

import java.util.List;

public interface TransactionMatchingCriteria {

    String getNodeName();

    List<TransactionStatus> getStatuses();

    boolean isAMatch(TransactionDetails tx);

    boolean isOneTimeMatch();
}
