package net.consensys.eventeum.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.block.tx.TransactionMonitoringBlockListener;
import net.consensys.eventeum.chain.block.tx.criteria.TransactionMatchingCriteria;
import net.consensys.eventeum.chain.block.tx.criteria.factory.TransactionMatchingCriteriaFactory;
import net.consensys.eventeum.chain.config.EventConfirmationConfig;
import net.consensys.eventeum.chain.factory.TransactionDetailsFactory;
import net.consensys.eventeum.chain.service.BlockCache;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.integration.broadcast.internal.EventeumEventBroadcaster;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import net.consensys.eventeum.repository.TransactionMonitoringSpecRepository;
import net.consensys.eventeum.service.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class DefaultTransactionMonitoringService implements TransactionMonitoringService {

    private ChainServicesContainer chainServices;

    private BlockchainEventBroadcaster broadcaster;

    private EventeumEventBroadcaster eventeumEventBroadcaster;

    private TransactionDetailsFactory transactionDetailsFactory;

    private EventConfirmationConfig confirmationConfig;

    private TransactionMonitoringSpecRepository transactionMonitoringRepo;

    private TransactionMonitoringBlockListener monitoringBlockListener;

    private TransactionMatchingCriteriaFactory matchingCriteriaFactory;

    private BlockCache blockCache;

    private Map<String, TransactionMonitor> transactionMonitors = new HashMap<>();

    @Autowired
    public DefaultTransactionMonitoringService(ChainServicesContainer chainServices,
                                               BlockchainEventBroadcaster broadcaster,
                                               EventeumEventBroadcaster eventeumEventBroadcaster,
                                               TransactionDetailsFactory transactionDetailsFactory,
                                               EventConfirmationConfig confirmationConfig,
                                               TransactionMonitoringSpecRepository transactionMonitoringRepo,
                                               TransactionMonitoringBlockListener monitoringBlockListener,
                                               TransactionMatchingCriteriaFactory matchingCriteriaFactory,
                                               BlockCache blockCache) {
        this.chainServices = chainServices;
        this.broadcaster = broadcaster;
        this.eventeumEventBroadcaster = eventeumEventBroadcaster;
        this.transactionDetailsFactory = transactionDetailsFactory;
        this.confirmationConfig = confirmationConfig;
        this.transactionMonitoringRepo = transactionMonitoringRepo;
        this.monitoringBlockListener = monitoringBlockListener;
        this.matchingCriteriaFactory = matchingCriteriaFactory;
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

        removeTransactionMonitorMatchinCriteria(transactionMonitor);
        deleteTransactionMonitor(monitorId);

        if (broadcast) {
            eventeumEventBroadcaster.broadcastTransactionMonitorRemoved(transactionMonitor.getSpec());
        }
    }

    private void removeTransactionMonitorMatchinCriteria(TransactionMonitor transactionMonitor) {
        monitoringBlockListener.removeMatchingCriteria(transactionMonitor.getMatchingCriteria());
    }

    private void deleteTransactionMonitor(String monitorId) {
        transactionMonitors.remove(monitorId);

        transactionMonitoringRepo.deleteById(monitorId);
    }

    private TransactionMonitor getTransactionMonitor(String monitorId) {
        return transactionMonitors.get(monitorId);
    }

    private void registerTransactionMonitoring(TransactionMonitoringSpec spec) {

        final TransactionMatchingCriteria matchingCriteria = matchingCriteriaFactory.build(spec);
        monitoringBlockListener.addMatchingCriteria(matchingCriteria);

        transactionMonitors.put(spec.getId(), new TransactionMonitor(spec, matchingCriteria));
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

        TransactionMatchingCriteria matchingCriteria;

        public TransactionMonitor(TransactionMonitoringSpec spec, TransactionMatchingCriteria matchingCriteria) {
            this.spec = spec;
            this.matchingCriteria = matchingCriteria;
        }

    }
}
