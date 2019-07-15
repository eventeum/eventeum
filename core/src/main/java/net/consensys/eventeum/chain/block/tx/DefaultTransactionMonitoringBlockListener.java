package net.consensys.eventeum.chain.block.tx;

import net.consensys.eventeum.chain.block.SelfUnregisteringBlockListener;
import net.consensys.eventeum.chain.config.EventConfirmationConfig;
import net.consensys.eventeum.chain.factory.TransactionDetailsFactory;
import net.consensys.eventeum.chain.service.BlockCache;
import net.consensys.eventeum.chain.service.BlockchainException;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.Transaction;
import net.consensys.eventeum.chain.service.domain.TransactionReceipt;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.dto.transaction.TransactionStatus;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import net.consensys.eventeum.service.AsyncTaskService;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultTransactionMonitoringBlockListener extends SelfUnregisteringBlockListener
        implements TransactionMonitoringBlockListener {

    private String nodeName;

    private List<TransactionMatchingCriteria> criteria;

    private BlockchainService blockchainService;

    private BlockchainEventBroadcaster broadcaster;

    private TransactionDetailsFactory transactionDetailsFactory;

    private EventConfirmationConfig confirmationConfig;

    private AsyncTaskService asyncService;

    private BlockCache blockCache;

    private RetryTemplate retryTemplate;

    private Lock lock = new ReentrantLock();

    public DefaultTransactionMonitoringBlockListener(String nodeName,
                                                     BlockchainService blockchainService,
                                                     BlockchainEventBroadcaster broadcaster,
                                                     TransactionDetailsFactory transactionDetailsFactory,
                                                     EventConfirmationConfig confirmationConfig,
                                                     AsyncTaskService asyncService,
                                                     BlockCache blockCache) {
        super(blockchainService);

        this.criteria = new CopyOnWriteArrayList<>();

        this.blockchainService = blockchainService;
        this.broadcaster = broadcaster;
        this.transactionDetailsFactory = transactionDetailsFactory;
        this.confirmationConfig = confirmationConfig;
        this.asyncService = asyncService;
        this.blockCache = blockCache;

        init();
    }

    @Override
    public void onBlock(BlockDetails blockDetails) {
        asyncService.execute(() -> {
            lock.lock();

            try {
                processBlock(blockDetails);
            } finally {
                lock.unlock();
            }
        });
    }

    @Override
    public void addMatchingCriteria(TransactionMatchingCriteria matchingCriteria) {
        criteria.add(matchingCriteria);
    }

    @Override
    public void removeMatchingCriteria(TransactionMatchingCriteria matchingCriteria) {
        criteria.remove(matchingCriteria);
    }

    protected RetryTemplate getRetryTemplate() {
        if (retryTemplate == null) {
            retryTemplate = new RetryTemplate();

            final FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
            fixedBackOffPolicy.setBackOffPeriod(500);
            retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

            final SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
            retryPolicy.setMaxAttempts(3);
            retryTemplate.setRetryPolicy(retryPolicy);
        }

        return retryTemplate;
    }

    private void init() {
        asyncService.execute(() -> {
            lock.lock();

            //Check cached blocks in case transaction has already been recently mined
            try {
                blockCache
                        .getCachedBlocks()
                        .forEach(blockDetails -> processBlock(blockDetails));
            } finally {
                lock.unlock();
            }
        });
    }

    private void processBlock(BlockDetails blockDetails) {
        getBlock(blockDetails.getHash())
                .ifPresent(block -> {
                    block.getTransactions().forEach(tx -> broadcastIfMatched(tx));
                });
    }

    private void broadcastIfMatched(Transaction tx) {
        final TransactionDetails txDetails = transactionDetailsFactory.createTransactionDetails(
                tx, TransactionStatus.CONFIRMED, nodeName);

        //Only broadcast once, even if multiple matching criteria apply
        criteria.stream()
                .filter(matcher -> matcher.isAMatch(txDetails))
                .findFirst()
                .ifPresent(matcher -> onTransactionMatched(txDetails));
    }

    private Optional<Block> getBlock(String blockHash) {
        return getRetryTemplate().execute((context) -> {
            final Optional<Block> block =  blockchainService.getBlock(blockHash, true);

            if (!block.isPresent()) {
                throw new BlockchainException("Block not found");
            }

            return block;
        });
    }

    private void onTransactionMatched(TransactionDetails txDetails) {

        final boolean isSuccess = isSuccessTransaction(txDetails);

        if (isSuccess && shouldWaitBeforeConfirmation()) {
            txDetails.setStatus(TransactionStatus.UNCONFIRMED);

            blockchainService.addBlockListener(new TransactionConfirmationBlockListener(txDetails,
                    blockchainService, broadcaster, confirmationConfig, asyncService, this));

            broadcaster.broadcastTransaction(txDetails);

            //Don't unregister if we're waiting for x blocks, as if there is a fork
            //we need to rebroadcast the unconfirmed tx in new block
            //The confirmation block listener will unregister this listened after confirmation
        } else {
            if (!isSuccess) {
                txDetails.setStatus(TransactionStatus.FAILED);
            }

            broadcaster.broadcastTransaction(txDetails);
            unregister();
        }
    }

    private boolean isSuccessTransaction(TransactionDetails txDetails) {
        final TransactionReceipt receipt = blockchainService.getTransactionReceipt(txDetails.getHash());

        if (receipt.getStatus() == null) {
            // status is only present on Byzantium transactions onwards
            return true;
        }

        if (receipt.getStatus().equals("0x0")) {
            return false;
        }

        return true;
    }

    private boolean shouldWaitBeforeConfirmation() {
        return !confirmationConfig.getBlocksToWaitForConfirmation().equals(BigInteger.ZERO);
    }
}
