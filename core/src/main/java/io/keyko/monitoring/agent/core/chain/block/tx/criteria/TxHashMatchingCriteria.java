package io.keyko.monitoring.agent.core.chain.block.tx.criteria;

import io.keyko.monitoring.agent.core.dto.transaction.TransactionDetails;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionStatus;

import java.util.List;

public class TxHashMatchingCriteria extends SingleValueMatchingCriteria<String> {

    public TxHashMatchingCriteria(String nodeName, String hashToMatch, List<TransactionStatus> statuses) {
        super(nodeName, hashToMatch, statuses);
    }

    @Override
    protected String getValueFromTx(TransactionDetails tx) {
        return tx.getHash();
    }

    @Override
    public boolean isOneTimeMatch() {
        return true;
    }
}
