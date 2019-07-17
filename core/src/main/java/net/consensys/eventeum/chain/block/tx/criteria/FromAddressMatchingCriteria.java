package net.consensys.eventeum.chain.block.tx.criteria;

import net.consensys.eventeum.dto.transaction.TransactionDetails;

import java.util.List;

public class FromAddressMatchingCriteria extends SingleValueMatchingCriteria<String> {

    public FromAddressMatchingCriteria(String nodeName, String fromAddress, List<String> statuses) {
        super(nodeName, fromAddress, statuses);
    }

    @Override
    protected String getValueFromTx(TransactionDetails tx) {
        return tx.getFrom();
    }

    @Override
    public boolean isOneTimeMatch() {
        return true;
    }

    @Override
    public boolean canBeRemoved() {
        return false;
    }
}
