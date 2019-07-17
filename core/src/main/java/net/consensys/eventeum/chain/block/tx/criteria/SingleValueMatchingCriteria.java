package net.consensys.eventeum.chain.block.tx.criteria;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.consensys.eventeum.dto.transaction.TransactionDetails;

import java.util.List;

@Data
@AllArgsConstructor
public abstract class SingleValueMatchingCriteria<T> implements TransactionMatchingCriteria {

    private String nodeName;

    private T valueToMatch;

    private List<String> statuses;

    @Override
    public boolean isAMatch(TransactionDetails tx) {
        return valueToMatch.equals(getValueFromTx(tx));
    }

    protected abstract T getValueFromTx(TransactionDetails tx);
}
