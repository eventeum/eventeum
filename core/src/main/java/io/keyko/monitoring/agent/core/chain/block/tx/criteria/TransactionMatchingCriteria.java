package io.keyko.monitoring.agent.core.chain.block.tx.criteria;

import io.keyko.monitoring.agent.core.dto.transaction.TransactionDetails;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionStatus;

import java.util.List;

public interface TransactionMatchingCriteria {

    String getNodeName();

    List<TransactionStatus> getStatuses();

    boolean isAMatch(TransactionDetails tx);

    boolean isOneTimeMatch();
}
