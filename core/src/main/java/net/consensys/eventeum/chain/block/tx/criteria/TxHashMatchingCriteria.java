package net.consensys.eventeum.chain.block.tx.criteria;

import net.consensys.eventeum.dto.transaction.TransactionDetails;

import java.util.List;

public class TxHashMatchingCriteria extends SingleValueMatchingCriteria<String> {

    public TxHashMatchingCriteria(String nodeName, String hashToMatch, List<String> statuses) {
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

    @Override
    public boolean canBeRemoved() {
        return true;
    }
}
