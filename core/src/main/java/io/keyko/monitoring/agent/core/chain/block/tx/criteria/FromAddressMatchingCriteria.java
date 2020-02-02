package io.keyko.monitoring.agent.core.chain.block.tx.criteria;

import io.keyko.monitoring.agent.core.dto.transaction.TransactionDetails;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionStatus;

import java.util.List;

public class FromAddressMatchingCriteria extends SingleValueMatchingCriteria<String> {

    public FromAddressMatchingCriteria(String nodeName, String fromAddress, List<TransactionStatus> statuses) {
        super(nodeName, fromAddress, statuses);
    }

    @Override
    protected String getValueFromTx(TransactionDetails tx) {
        return tx.getFrom();
    }

    @Override
    public boolean isOneTimeMatch() {
        return false;
    }
}
