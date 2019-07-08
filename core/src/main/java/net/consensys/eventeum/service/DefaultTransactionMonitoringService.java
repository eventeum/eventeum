package net.consensys.eventeum.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.block.TransactionMonitoringBlockListener;
import net.consensys.eventeum.chain.config.EventConfirmationConfig;
import net.consensys.eventeum.chain.factory.TransactionDetailsFactory;
import net.consensys.eventeum.chain.service.BlockCache;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.domain.Transaction;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.integration.broadcast.internal.EventeumEventBroadcaster;
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

    private EventeumEventBroadcaster eventeumEventBroadcaster;

    private TransactionDetailsFactory transactionDetailsFactory;

    private EventConfirmationConfig confirmationConfig;

    private AsyncTaskService asyncService;

    private TransactionMonitoringSpecRepository transactionMonitoringRepo;

    private BlockCache blockCache;

    private Map<String, TransactionMonitor> transactionMonitors = new HashMap<>();

    @Autowired
    public DefaultTransactionMonitoringService(ChainServicesContainer chainServices,
                                               BlockchainEventBroadcaster broadcaster,
                                               EventeumEventBroadcaster eventeumEventBroadcaster,
                                               TransactionDetailsFactory transactionDetailsFactory,
                                               EventConfirmationConfig confirmationConfig,
                                               AsyncTaskService asyncService,
                                               TransactionMonitoringSpecRepository transactionMonitoringRepo,
                                               BlockCache blockCache) {
        this.chainServices = chainServices;
        this.broadcaster = broadcaster;
        this.eventeumEventBroadcaster = eventeumEventBroadcaster;
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

        if (broadcast) {
            eventeumEventBroadcaster.broadcastTransactionMonitorAdded(spec);
        }
    }

    @Override
    public void stopMonitoringTransactions(String monitorId) throws NotFoundException {
        stopMonitoringTransactions(monitorId, true);
    }

    @Override
    public void stopMonitoringTransactions(String monitorId, boolean broadcast) throws NotFoundException {

        final TransactionMonitor transactionMonitor = getTransactionMonitor(monitorId);

        if (transactionMonitor == null) {
            throw new NotFoundException("No monitored transaction with id: " + monitorId);
        }

        removeTransactionMonitorListeners(transactionMonitor);
        deleteTransactionMonitor(monitorId);

        if (broadcast) {
            eventeumEventBroadcaster.broadcastTransactionMonitorRemoved(transactionMonitor.getSpec());
        }
    }

    private void removeTransactionMonitorListeners(TransactionMonitor transactionMonitor) {
        final BlockchainService blockchainService = chainServices.getNodeServices(
                transactionMonitor.getSpec().getNodeName()).getBlockchainService();

        transactionMonitor
                .getBlockListeners()
                .forEach(listener -> blockchainService.removeBlockListener(listener));
    }

    private void deleteTransactionMonitor(String monitorId) {
        transactionMonitors.remove(monitorId);

        transactionMonitoringRepo.deleteById(monitorId);
    }

    private TransactionMonitor getTransactionMonitor(String monitorId) {
        return transactionMonitors.get(monitorId);
    }

    private void registerTransactionMonitoring(TransactionMonitoringSpec spec) {
        final BlockchainService blockchainService = chainServices.getNodeServices(
                spec.getNodeName()).getBlockchainService();

        final TransactionMonitoringBlockListener monitoringBlockListener =
                new TransactionMonitoringBlockListener(spec, blockchainService,
                        broadcaster, transactionDetailsFactory, confirmationConfig, asyncService, blockCache);

        blockchainService.addBlockListener(monitoringBlockListener);

        transactionMonitors.put(spec.getId(),
                new TransactionMonitor(spec, Collections.singleton(monitoringBlockListener)));
    }

    private TransactionMonitoringSpec saveTransactionMonitoringSpec(TransactionMonitoringSpec spec) {
        return transactionMonitoringRepo.save(spec);
    }

    private boolean isTransactionSpecRegistered(TransactionMonitoringSpec spec) {
        return transactionMonitors.containsKey(spec.getId());
    }

    @Data
    private class TransactionMonitor {
        TransactionMonitoringSpec spec;

        Set<BlockListener> blockListeners;

        public TransactionMonitor(TransactionMonitoringSpec spec, Set<BlockListener> blockListeners) {
            this.spec = spec;
            this.blockListeners = blockListeners;
        }

    }
}
