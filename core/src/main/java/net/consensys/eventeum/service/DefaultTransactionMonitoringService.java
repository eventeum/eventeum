package net.consensys.eventeum.service;

import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.block.SelfUnregisteringBlockListener;
import net.consensys.eventeum.chain.block.TransactionMonitoringBlockListener;
import net.consensys.eventeum.chain.factory.TransactionDetailsFactory;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.dto.transaction.TransactionIdentifier;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.service.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Service
public class DefaultTransactionMonitoringService implements TransactionMonitoringService {

    private ChainServicesContainer chainServices;

    private BlockchainEventBroadcaster broadcaster;

    private TransactionDetailsFactory transactionDetailsFactory;

    private Map<String, Set<BlockListener>> monitoredTransactions;

    @Override
    public void registerTransactionToMonitor(TransactionIdentifier identifier) {
        final BlockchainService blockchainService = chainServices.getNodeServices(
                identifier.getNodeName()).getBlockchainService();

        final TransactionMonitoringBlockListener monitoringBlockListener =
                new TransactionMonitoringBlockListener(
                        identifier, blockchainService, broadcaster, transactionDetailsFactory);

        blockchainService.addBlockListener(monitoringBlockListener);

        monitoredTransactions.put(identifier.toString(), Collections.singleton(monitoringBlockListener));
    }

    @Override
    public void stopMonitoringTransaction(TransactionIdentifier identifier) throws NotFoundException {
        if (!monitoredTransactions.containsKey(identifier.toString())) {
            throw new NotFoundException("No monitored transaction with id: " + identifier.toString());
        }

        final BlockchainService blockchainService = chainServices.getNodeServices(
                identifier.getNodeName()).getBlockchainService();

        monitoredTransactions
                .get(identifier.toString())
                .forEach(listener -> blockchainService.removeBlockListener(listener));

        monitoredTransactions.remove(identifier.toString());
    }
}
