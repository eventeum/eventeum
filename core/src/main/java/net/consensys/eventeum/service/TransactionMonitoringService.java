package net.consensys.eventeum.service;

import net.consensys.eventeum.dto.transaction.TransactionIdentifier;
import net.consensys.eventeum.service.exception.NotFoundException;

public interface TransactionMonitoringService {

    void registerTransactionToMonitor(TransactionIdentifier identifier);

    void stopMonitoringTransaction(TransactionIdentifier identifier) throws NotFoundException;
}
