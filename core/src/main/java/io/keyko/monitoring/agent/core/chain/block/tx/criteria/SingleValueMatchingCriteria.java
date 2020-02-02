package io.keyko.monitoring.agent.core.chain.block.tx.criteria;

import lombok.AllArgsConstructor;
import lombok.Data;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionDetails;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionStatus;

import java.util.List;

@Data
@AllArgsConstructor
public abstract class SingleValueMatchingCriteria<T> implements TransactionMatchingCriteria {

    private String nodeName;

    private T valueToMatch;

    private List<TransactionStatus> statuses;

    @Override
    public boolean isAMatch(TransactionDetails tx) {
        return valueToMatch.equals(getValueFromTx(tx));
    }

    protected abstract T getValueFromTx(TransactionDetails tx);
}
