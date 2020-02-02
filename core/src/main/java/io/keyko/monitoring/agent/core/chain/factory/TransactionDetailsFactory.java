package io.keyko.monitoring.agent.core.chain.factory;

import io.keyko.monitoring.agent.core.chain.service.domain.Transaction;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionDetails;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionStatus;

public interface TransactionDetailsFactory {
    TransactionDetails createTransactionDetails(
            Transaction transaction, TransactionStatus status, String nodeName);
}
