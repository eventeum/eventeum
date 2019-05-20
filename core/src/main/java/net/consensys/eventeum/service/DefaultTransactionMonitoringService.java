package net.consensys.eventeum.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.block.TransactionMonitoringBlockListener;
import net.consensys.eventeum.chain.config.EventConfirmationConfig;
import net.consensys.eventeum.chain.factory.TransactionDetailsFactory;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import net.consensys.eventeum.service.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class DefaultTransactionMonitoringService implements TransactionMonitoringService {

    private ChainServicesContainer chainServices;

    private BlockchainEventBroadcaster broadcaster;

    private TransactionDetailsFactory transactionDetailsFactory;

    private EventConfirmationConfig confirmationConfig;

    private AsyncTaskService asyncService;

    private Map<String, MonitoredTransaction> monitoredTransactions = new HashMap<>();

    @Autowired
    public DefaultTransactionMonitoringService(ChainServicesContainer chainServices,
                                               BlockchainEventBroadcaster broadcaster,
                                               TransactionDetailsFactory transactionDetailsFactory,
                                               EventConfirmationConfig confirmationConfig,
                                               AsyncTaskService asyncService) {
        this.chainServices = chainServices;
        this.broadcaster = broadcaster;
        this.transactionDetailsFactory = transactionDetailsFactory;
        this.confirmationConfig = confirmationConfig;
        this.asyncService = asyncService;
    }

    @Override
    public void registerTransactionsToMonitor(TransactionMonitoringSpec spec) {
        final BlockchainService blockchainService = chainServices.getNodeServices(
                spec.getNodeName()).getBlockchainService();

        final TransactionMonitoringBlockListener monitoringBlockListener =
                new TransactionMonitoringBlockListener(spec,
                        blockchainService, broadcaster, transactionDetailsFactory, confirmationConfig, asyncService);

        blockchainService.addBlockListener(monitoringBlockListener);

        monitoredTransactions.put(spec.getId(),
                new MonitoredTransaction(spec, Collections.singleton(monitoringBlockListener)));
    }

    @Override
    public void stopMonitoringTransactions(String specId) throws NotFoundException {
        if (!monitoredTransactions.containsKey(specId)) {
            throw new NotFoundException("No monitored transaction with id: " + specId);
        }

        final MonitoredTransaction monitoredTransaction = monitoredTransactions.get(specId);

        final BlockchainService blockchainService = chainServices.getNodeServices(
                monitoredTransaction.getSpec().getNodeName()).getBlockchainService();

        monitoredTransactions
                .get(specId)
                .getBlockListeners()
                .forEach(listener -> blockchainService.removeBlockListener(listener));

        monitoredTransactions.remove(specId);
    }

    @Data
    private class MonitoredTransaction {
        TransactionMonitoringSpec spec;

        Set<BlockListener> blockListeners;

        public MonitoredTransaction(TransactionMonitoringSpec spec, Set<BlockListener> blockListeners) {
            this.spec = spec;
            this.blockListeners = blockListeners;
        }

    }
}
