package net.consensys.eventeum.chain.block;

import net.consensys.eventeum.chain.config.EventConfirmationConfig;
import net.consensys.eventeum.chain.factory.TransactionDetailsFactory;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.Transaction;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.dto.transaction.TransactionIdentifier;
import net.consensys.eventeum.dto.transaction.TransactionStatus;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import net.consensys.eventeum.service.AsyncTaskService;

import java.util.Optional;

public class TransactionMonitoringBlockListener extends SelfUnregisteringBlockListener {

    private TransactionMonitoringSpec spec;

    private BlockchainService blockchainService;

    private BlockchainEventBroadcaster broadcaster;

    private TransactionDetailsFactory transactionDetailsFactory;

    private EventConfirmationConfig confirmationConfig;

    private AsyncTaskService asyncService;

    public TransactionMonitoringBlockListener(TransactionMonitoringSpec spec,
                                              BlockchainService blockchainService,
                                              BlockchainEventBroadcaster broadcaster,
                                              TransactionDetailsFactory transactionDetailsFactory,
                                              EventConfirmationConfig confirmationConfig,
                                              AsyncTaskService asyncService) {
        super(blockchainService);

        this.spec = spec;
        this.blockchainService = blockchainService;
        this.broadcaster = broadcaster;
        this.transactionDetailsFactory = transactionDetailsFactory;
        this.confirmationConfig = confirmationConfig;
        this.asyncService = asyncService;
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

        final TransactionDetails txDetails = transactionDetailsFactory.createTransactionDetails(
                tx, TransactionStatus.UNCONFIRMED, spec.getNodeName());

        broadcaster.broadcastTransaction(txDetails);

        blockchainService.addBlockListener(new TransactionConfirmationBlockListener(txDetails,
                blockchainService, broadcaster, confirmationConfig, asyncService));

        unregister();
    }

    private Optional<Transaction> getTransaction(Block block) {

        return block
                .getTransactions()
                .stream()
                .filter(tx -> tx.getHash().equals(spec.getTransactionIdentifier()))
                .findFirst();
    }
}
