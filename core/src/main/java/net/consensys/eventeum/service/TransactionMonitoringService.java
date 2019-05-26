package net.consensys.eventeum.service;

import net.consensys.eventeum.dto.transaction.TransactionIdentifier;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import net.consensys.eventeum.service.exception.NotFoundException;

public interface TransactionMonitoringService {

    void registerTransactionsToMonitor(TransactionMonitoringSpec spec);

    void registerTransactionsToMonitor(TransactionMonitoringSpec spec, boolean broadcast);

    void stopMonitoringTransactions(String id) throws NotFoundException;

    void stopMonitoringTransactions(String id, boolean broadcast) throws NotFoundException;
}
