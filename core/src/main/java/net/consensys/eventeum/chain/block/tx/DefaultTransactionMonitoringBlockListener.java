package net.consensys.eventeum.chain.block.tx;

import net.consensys.eventeum.chain.block.tx.criteria.TransactionMatchingCriteria;
import net.consensys.eventeum.chain.config.EventConfirmationConfig;
import net.consensys.eventeum.chain.factory.TransactionDetailsFactory;
import net.consensys.eventeum.chain.service.BlockCache;
import net.consensys.eventeum.chain.service.BlockchainException;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.Transaction;
import net.consensys.eventeum.chain.service.domain.TransactionReceipt;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.dto.transaction.TransactionStatus;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.service.AsyncTaskService;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class DefaultTransactionMonitoringBlockListener implements TransactionMonitoringBlockListener {

    //Keyed by node name
    private Map<String, List<TransactionMatchingCriteria>> criteria;

    //Keys by node name
    private Map<String, BlockchainService> blockchainServices;

    private BlockchainEventBroadcaster broadcaster;

    private TransactionDetailsFactory transactionDetailsFactory;

    private EventConfirmationConfig confirmationConfig;

    private AsyncTaskService asyncService;

    private BlockCache blockCache;

    private RetryTemplate retryTemplate;

    private Lock lock = new ReentrantLock();

    public DefaultTransactionMonitoringBlockListener(ChainServicesContainer chainServicesContainer,
                                                     BlockchainEventBroadcaster broadcaster,
                                                     TransactionDetailsFactory transactionDetailsFactory,
                                                     EventConfirmationConfig confirmationConfig,
                                                     AsyncTaskService asyncService,
                                                     BlockCache blockCache) {
        this.criteria = new ConcurrentHashMap<>();

        this.blockchainServices = new HashMap<>();

        chainServicesContainer
                .getNodeNames()
                .forEach(nodeName -> {
                    blockchainServices.put(nodeName,
                            chainServicesContainer.getNodeServices(nodeName).getBlockchainService());
                });

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

        final String nodeName = matchingCriteria.getNodeName();

        if (!criteria.containsKey(nodeName)) {
            criteria.put(nodeName, new CopyOnWriteArrayList<>());
        }

        criteria.get(nodeName).add(matchingCriteria);
    }

    @Override
    public void removeMatchingCriteria(TransactionMatchingCriteria matchingCriteria) {
        criteria.get(matchingCriteria.getNodeName()).remove(matchingCriteria);
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
        getBlock(blockDetails.getHash(), blockDetails.getNodeName())
                .ifPresent(block -> {
                    block.getTransactions().forEach(tx -> broadcastIfMatched(tx, blockDetails.getNodeName()));
                });
    }

    private void broadcastIfMatched(Transaction tx, String nodeName) {
        if (criteria.containsKey(nodeName)) {
            final TransactionDetails txDetails = transactionDetailsFactory.createTransactionDetails(
                    tx, TransactionStatus.CONFIRMED, nodeName);

            //Only broadcast once, even if multiple matching criteria apply
            criteria.get(nodeName)
                    .stream()
                    .filter(matcher -> matcher.isAMatch(txDetails))
                    .findFirst()
                    .ifPresent(matcher -> onTransactionMatched(txDetails, matcher));
        }
    }

    private Optional<Block> getBlock(String blockHash, String nodeName) {
        return getRetryTemplate().execute((context) -> {
            final Optional<Block> block =  getBlockchainService(nodeName).getBlock(blockHash, true);

            if (!block.isPresent()) {
                throw new BlockchainException("Block not found");
            }

            return block;
        });
    }

    private void onTransactionMatched(TransactionDetails txDetails, TransactionMatchingCriteria matchingCriteria) {

        final BlockchainService blockchainService = getBlockchainService(txDetails.getNodeName());

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

            if (matchingCriteria.isOneTimeMatch()) {
                removeMatchingCriteria(matchingCriteria);
            }
        }
    }

    private boolean isSuccessTransaction(TransactionDetails txDetails) {
        final TransactionReceipt receipt = getBlockchainService(txDetails.getNodeName())
                .getTransactionReceipt(txDetails.getHash());

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

    private BlockchainService getBlockchainService(String nodeName) {
        return blockchainServices.get(nodeName);
    }
}
