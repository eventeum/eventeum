package io.keyko.monitoring.agent.core.service;

import io.keyko.monitoring.agent.core.model.TransactionMonitoringSpec;
import io.keyko.monitoring.agent.core.service.exception.NotFoundException;

public interface TransactionMonitoringService {

    void registerTransactionsToMonitor(TransactionMonitoringSpec spec);

    void registerTransactionsToMonitor(TransactionMonitoringSpec spec, boolean broadcast);

    void stopMonitoringTransactions(String id) throws NotFoundException;

    void stopMonitoringTransactions(String id, boolean broadcast) throws NotFoundException;
}
