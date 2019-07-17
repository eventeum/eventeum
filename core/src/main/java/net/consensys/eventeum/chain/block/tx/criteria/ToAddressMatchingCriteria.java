package net.consensys.eventeum.chain.block.tx.criteria;

import net.consensys.eventeum.dto.transaction.TransactionDetails;

import java.util.List;

public class ToAddressMatchingCriteria extends SingleValueMatchingCriteria<String> {

    public ToAddressMatchingCriteria(String nodeName, String toAddress, List<String> statuses) {
        super(nodeName, toAddress, statuses);
    }

    @Override
    protected String getValueFromTx(TransactionDetails tx) {
        return tx.getTo();
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
