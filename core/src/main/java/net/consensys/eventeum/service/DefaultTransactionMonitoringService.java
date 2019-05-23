package net.consensys.eventeum.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.block.TransactionMonitoringBlockListener;
import net.consensys.eventeum.chain.config.EventConfirmationConfig;
import net.consensys.eventeum.chain.factory.TransactionDetailsFactory;
import net.consensys.eventeum.chain.service.BlockCache;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import net.consensys.eventeum.repository.TransactionMonitoringSpecRepository;
import net.consensys.eventeum.service.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class DefaultTransactionMonitoringService implements TransactionMonitoringService {

    private ChainServicesContainer chainServices;

    private BlockchainEventBroadcaster broadcaster;

    private TransactionDetailsFactory transactionDetailsFactory;

    private EventConfirmationConfig confirmationConfig;

    private AsyncTaskService asyncService;

    private TransactionMonitoringSpecRepository transactionMonitoringRepo;

    private BlockCache blockCache;

    private Map<String, MonitoredTransaction> monitoredTransactions = new HashMap<>();

    @Autowired
    public DefaultTransactionMonitoringService(ChainServicesContainer chainServices,
                                               BlockchainEventBroadcaster broadcaster,
                                               TransactionDetailsFactory transactionDetailsFactory,
                                               EventConfirmationConfig confirmationConfig,
                                               AsyncTaskService asyncService,
                                               TransactionMonitoringSpecRepository transactionMonitoringRepo,
                                               BlockCache blockCache) {
        this.chainServices = chainServices;
        this.broadcaster = broadcaster;
        this.transactionDetailsFactory = transactionDetailsFactory;
        this.confirmationConfig = confirmationConfig;
        this.asyncService = asyncService;
        this.transactionMonitoringRepo = transactionMonitoringRepo;
        this.blockCache = blockCache;
    }

    @Override
    public void registerTransactionsToMonitor(TransactionMonitoringSpec spec) {
        registerTransactionsToMonitor(spec, true);
    }

    @Override
    public void registerTransactionsToMonitor(TransactionMonitoringSpec spec, boolean broadcast) {
        if (isTransactionSpecRegistered(spec)) {
            log.info("Already registered transaction monitoring spec with id: " + spec.getId());
            return;
        }

        registerTransactionMonitoring(spec);
        saveTransactionMonitoringSpec(spec);
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

    private void registerTransactionMonitoring(TransactionMonitoringSpec spec) {
        final BlockchainService blockchainService = chainServices.getNodeServices(
                spec.getNodeName()).getBlockchainService();

        final TransactionMonitoringBlockListener monitoringBlockListener =
                new TransactionMonitoringBlockListener(spec, blockchainService,
                        broadcaster, transactionDetailsFactory, confirmationConfig, asyncService, blockCache);

        blockchainService.addBlockListener(monitoringBlockListener);

        monitoredTransactions.put(spec.getId(),
                new MonitoredTransaction(spec, Collections.singleton(monitoringBlockListener)));
    }

    private TransactionMonitoringSpec saveTransactionMonitoringSpec(TransactionMonitoringSpec spec) {
        return transactionMonitoringRepo.save(spec);
    }

    private boolean isTransactionSpecRegistered(TransactionMonitoringSpec spec) {
        return monitoredTransactions.containsKey(spec.getId());
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
