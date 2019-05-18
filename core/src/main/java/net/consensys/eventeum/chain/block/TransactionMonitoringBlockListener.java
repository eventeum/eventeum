package net.consensys.eventeum.chain.block;

import net.consensys.eventeum.chain.factory.TransactionDetailsFactory;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.Transaction;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.transaction.TransactionIdentifier;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;

import java.util.Optional;

public class TransactionMonitoringBlockListener extends SelfUnregisteringBlockListener {

    private TransactionIdentifier transactionIdentifier;

    private BlockchainService blockchainService;

    private BlockchainEventBroadcaster broadcaster;

    private TransactionDetailsFactory transactionDetailsFactory;

    public TransactionMonitoringBlockListener(TransactionIdentifier transactionIdentifier,
                                              BlockchainService blockchainService,
                                              BlockchainEventBroadcaster broadcaster,
                                              TransactionDetailsFactory transactionDetailsFactory) {
        super(blockchainService);

        this.transactionIdentifier = transactionIdentifier;
        this.blockchainService = blockchainService;
        this.broadcaster = broadcaster;
        this.transactionDetailsFactory = transactionDetailsFactory;
    }

    @Override
    public void onBlock(BlockDetails blockDetails) {
        blockchainService
                .getBlock(blockDetails.getHash(), true)
                .ifPresent(block -> {
                    getTransaction(block).ifPresent(tx -> onTransactionMined(tx, block));
                });
    }

    private void onTransactionMined(Transaction tx, Block minedBlock) {
        broadcaster.broadcastTransaction(transactionDetailsFactory.createTransactionDetails(
                tx, transactionIdentifier.getNodeName()));

        unregister();
    }

    private Optional<Transaction> getTransaction(Block block) {

        return block
                .getTransactions()
                .stream()
                .filter(tx -> tx.getHash().equals(transactionIdentifier.getHash()))
                .findFirst();
    }
}
