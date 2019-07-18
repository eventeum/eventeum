package net.consensys.eventeum.chain.block.tx.criteria.factory;

import net.consensys.eventeum.chain.block.tx.criteria.FromAddressMatchingCriteria;
import net.consensys.eventeum.chain.block.tx.criteria.ToAddressMatchingCriteria;
import net.consensys.eventeum.chain.block.tx.criteria.TransactionMatchingCriteria;
import net.consensys.eventeum.chain.block.tx.criteria.TxHashMatchingCriteria;
import net.consensys.eventeum.model.TransactionIdentifierType;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
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
