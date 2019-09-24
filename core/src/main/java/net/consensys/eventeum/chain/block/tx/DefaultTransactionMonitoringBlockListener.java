package net.consensys.eventeum.chain.block.tx;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class DefaultTransactionMonitoringBlockListener implements TransactionMonitoringBlockListener {

    //Keyed by node name
    private Map<String, List<TransactionMatchingCriteria>> criteria;

    //Keyed by node name
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
    }

    @Override
    public void onBlock(Block block) {
        asyncService.execute(() -> {
            lock.lock();

            try {
                processBlock(block);
            } finally {
                lock.unlock();
            }
        });
    }

    @Override
    public void addMatchingCriteria(TransactionMatchingCriteria matchingCriteria) {

        lock.lock();

        try {
            final String nodeName = matchingCriteria.getNodeName();

            if (!criteria.containsKey(nodeName)) {
                criteria.put(nodeName, new CopyOnWriteArrayList<>());
            }

            criteria.get(nodeName).add(matchingCriteria);

            //Check if any cached blocks match
            //Note, this makes sense for tx hash but maybe doesn't for some other matchers?
            blockCache
                    .getCachedBlocks()
                    .forEach(block -> {
                        block.getTransactions().forEach(tx ->
                                broadcastIfMatched(tx, nodeName, Collections.singletonList(matchingCriteria)));
                    });
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeMatchingCriteria(TransactionMatchingCriteria matchingCriteria) {
        criteria.get(matchingCriteria.getNodeName()).remove(matchingCriteria);
    }

    private void processBlock(Block block) {
        block.getTransactions()
                .forEach(tx -> broadcastIfMatched(tx, block.getNodeName()));
    }

    private void broadcastIfMatched(Transaction tx, String nodeName, List<TransactionMatchingCriteria> criteriaToCheck) {

        final TransactionDetails txDetails = transactionDetailsFactory.createTransactionDetails(
                tx, TransactionStatus.CONFIRMED, nodeName);

        //Only broadcast once, even if multiple matching criteria apply
        criteriaToCheck
                .stream()
                .filter(matcher -> matcher.isAMatch(txDetails))
                .findFirst()
                .ifPresent(matcher -> onTransactionMatched(txDetails, matcher));
    }

    private void broadcastIfMatched(Transaction tx, String nodeName) {
        if (criteria.containsKey(nodeName)) {
            broadcastIfMatched(tx, nodeName, criteria.get(nodeName));
        }
    }

    private void onTransactionMatched(TransactionDetails txDetails, TransactionMatchingCriteria matchingCriteria) {

        final BlockchainService blockchainService = getBlockchainService(txDetails.getNodeName());

        final boolean isSuccess = isSuccessTransaction(txDetails);

        if (isSuccess && shouldWaitBeforeConfirmation()) {
            txDetails.setStatus(TransactionStatus.UNCONFIRMED);

            blockchainService.addBlockListener(new TransactionConfirmationBlockListener(txDetails,
                    blockchainService, broadcaster, confirmationConfig, asyncService,
                    matchingCriteria.getStatuses(),
                    () -> onConfirmed(txDetails, matchingCriteria)));

            broadcastTransaction(txDetails, matchingCriteria);

            //Don't remove criteria if we're waiting for x blocks, as if there is a fork
            //we need to rebroadcast the unconfirmed tx in new block
        } else {
            if (!isSuccess) {
                txDetails.setStatus(TransactionStatus.FAILED);
            }

            broadcastTransaction(txDetails, matchingCriteria);

            if (matchingCriteria.isOneTimeMatch()) {
                removeMatchingCriteria(matchingCriteria);
            }
        }
    }

    private void broadcastTransaction(TransactionDetails txDetails, TransactionMatchingCriteria matchingCriteria) {
        if (matchingCriteria.getStatuses().contains(txDetails.getStatus())) {
            broadcaster.broadcastTransaction(txDetails);
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

    private void onConfirmed(TransactionDetails txDetails, TransactionMatchingCriteria matchingCriteria) {
        if (matchingCriteria.isOneTimeMatch()) {
            log.debug("Tx {} confirmed, removing matchingCriteria", txDetails.getHash());

            removeMatchingCriteria(matchingCriteria);
        }
    }
}
