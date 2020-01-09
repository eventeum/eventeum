package net.consensys.eventeum.chain.factory;

import net.consensys.eventeum.chain.service.domain.Transaction;
import net.consensys.eventeum.TransactionDetails;
import net.consensys.eventeum.TransactionStatus;

public interface TransactionDetailsFactory {
    TransactionDetails createTransactionDetails(
            Transaction transaction, TransactionStatus status, String nodeName);
}
