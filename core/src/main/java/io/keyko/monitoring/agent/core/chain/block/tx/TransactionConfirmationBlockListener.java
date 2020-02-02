package io.keyko.monitoring.agent.core.chain.block.tx;

import io.keyko.monitoring.agent.core.chain.service.domain.TransactionReceipt;
import io.keyko.monitoring.agent.core.chain.block.SelfUnregisteringBlockListener;
import io.keyko.monitoring.agent.core.chain.service.BlockchainService;
import io.keyko.monitoring.agent.core.chain.service.domain.Block;
import io.keyko.monitoring.agent.core.chain.settings.Node;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionDetails;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionStatus;
import io.keyko.monitoring.agent.core.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TransactionConfirmationBlockListener extends SelfUnregisteringBlockListener {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionConfirmationBlockListener.class);

    private TransactionDetails transactionDetails;
    private BlockchainService blockchainService;
    private BlockchainEventBroadcaster eventBroadcaster;
    private BigInteger targetBlock;
    private BigInteger blocksToWaitForMissingTx;
    private OnConfirmedCallback onConfirmedCallback;
    private AtomicBoolean isInvalidated = new AtomicBoolean(false);
    private BigInteger missingTxBlockLimit;
    private List<TransactionStatus> statusesToFilter;

    public TransactionConfirmationBlockListener(TransactionDetails transactionDetails,
                                                BlockchainService blockchainService,
                                                BlockchainEventBroadcaster eventBroadcaster,
                                                Node node,
                                                List<TransactionStatus> statusesToFilter,
                                                OnConfirmedCallback onConfirmedCallback) {
        super(blockchainService);
        this.transactionDetails = transactionDetails;
        this.blockchainService = blockchainService;
        this.eventBroadcaster = eventBroadcaster;
        this.onConfirmedCallback = onConfirmedCallback;
        this.statusesToFilter = statusesToFilter;

        final BigInteger currentBlock = blockchainService.getCurrentBlockNumber();
        this.targetBlock = currentBlock.add(node.getBlocksToWaitForConfirmation());
        this.blocksToWaitForMissingTx = node.getBlocksToWaitForMissingTx();
    }

    @Override
    public void onBlock(Block block) {
        final TransactionReceipt receipt = blockchainService.getTransactionReceipt(transactionDetails.getHash());

        if (receipt == null) {
            //Tx has disappeared...we've probably forked
            //Tx should be included in block on new fork soon
            handleMissingTransaction(block);
            return;
        }

        checkTransactionStatus(block.getNumber(), receipt);
    }

    private void checkTransactionStatus(BigInteger currentBlockNumber, TransactionReceipt receipt) {
        if (isOrphanedTransaction(receipt)) {
            processInvalidatedEvent();
        } else if (currentBlockNumber.compareTo(targetBlock) >= 0) {
            LOG.debug("Target block reached for transaction: {}", transactionDetails.getHash());
            broadcastTransactionConfirmed();
            unregister();
        }
    }

    private void processInvalidatedEvent() {
        broadcastTransactionInvalidated();
        isInvalidated.set(true);
        unregister();
    }

    private boolean isOrphanedTransaction(TransactionReceipt receipt) {
        //If block hash or log index are not as expected, this means that the transaction
        //associated with the event has been included in a block on a different fork of a longer chain
        //and the original event is considered orphaned.
        String orphanReason = null;

        if (!receipt.getBlockHash().equals(transactionDetails.getBlockHash())) {
            orphanReason = "Expected blockhash " + transactionDetails.getBlockHash() + ", received " + receipt.getBlockHash();
        }

        if (orphanReason != null) {
            LOG.info("Orphan event detected: " + orphanReason);
            return true;
        }

        return false;
    }

    private void broadcastTransactionInvalidated() {
        transactionDetails.setStatus(TransactionStatus.INVALIDATED);
        broadcastEvent(transactionDetails);
    }

    private void broadcastTransactionConfirmed() {
        transactionDetails.setStatus(TransactionStatus.CONFIRMED);
        broadcastEvent(transactionDetails);

        onConfirmedCallback.onConfirmed();
    }

    private void broadcastEvent(TransactionDetails transactionDetails) {
        if (!isInvalidated.get() && statusesToFilter.contains(transactionDetails.getStatus())) {
            LOG.debug(String.format("Sending confirmed event for transaction: %s", transactionDetails.getHash()));
            eventBroadcaster.broadcastTransaction(transactionDetails);
        }
    }

    private void handleMissingTransaction(Block block) {
        if (missingTxBlockLimit == null) {
            missingTxBlockLimit = block.getNumber().add(blocksToWaitForMissingTx);
        } else if (block.getNumber().compareTo(missingTxBlockLimit) > 0) {
            processInvalidatedEvent();
        }
    }

    public interface OnConfirmedCallback {
        void onConfirmed();
    }
}
