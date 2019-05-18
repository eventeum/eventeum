package net.consensys.eventeum.chain.factory;

import net.consensys.eventeum.chain.service.domain.Transaction;
import net.consensys.eventeum.dto.transaction.TransactionDetails;

public interface TransactionDetailsFactory {
    TransactionDetails createTransactionDetails(Transaction transaction, String nodeName);
}
