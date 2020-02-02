package io.keyko.monitoring.agent.core.chain.block.tx.criteria.factory;

import io.keyko.monitoring.agent.core.chain.block.tx.criteria.FromAddressMatchingCriteria;
import io.keyko.monitoring.agent.core.chain.block.tx.criteria.ToAddressMatchingCriteria;
import io.keyko.monitoring.agent.core.chain.block.tx.criteria.TransactionMatchingCriteria;
import io.keyko.monitoring.agent.core.chain.block.tx.criteria.TxHashMatchingCriteria;
import io.keyko.monitoring.agent.core.model.TransactionIdentifierType;
import io.keyko.monitoring.agent.core.model.TransactionMonitoringSpec;
import org.springframework.stereotype.Component;

@Component
public class DefaultTransactionMatchingCriteriaFactory implements TransactionMatchingCriteriaFactory {

    @Override
    public TransactionMatchingCriteria build(TransactionMonitoringSpec spec) {
        if (spec.getType() == TransactionIdentifierType.HASH) {
            return new TxHashMatchingCriteria(spec.getNodeName(), spec.getTransactionIdentifierValue(), spec.getStatuses());
        }

        if (spec.getType() == TransactionIdentifierType.TO_ADDRESS) {
            return new ToAddressMatchingCriteria(spec.getNodeName(), spec.getTransactionIdentifierValue(), spec.getStatuses());
        }

        if (spec.getType() == TransactionIdentifierType.FROM_ADDRESS) {
            return new FromAddressMatchingCriteria(spec.getNodeName(), spec.getTransactionIdentifierValue(), spec.getStatuses());
        }

        throw new UnsupportedOperationException("Type: " + spec.getType() + " not currently supported");
    }
}
