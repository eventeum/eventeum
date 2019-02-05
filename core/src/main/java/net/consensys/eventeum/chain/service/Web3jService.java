package net.consensys.eventeum.chain.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.domain.TransactionReceipt;
import net.consensys.eventeum.chain.service.strategy.BlockSubscriptionStrategy;
import net.consensys.eventeum.chain.util.Web3jUtil;
import net.consensys.eventeum.chain.service.domain.wrapper.Web3jTransactionReceipt;
import net.consensys.eventeum.chain.service.factory.ContractEventDetailsFactory;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.service.AsyncTaskService;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.contract.ContractEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.filters.FilterException;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.*;
import rx.Observable;
import rx.Subscription;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A BlockchainService implementating utilising the Web3j library.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Slf4j
public class Web3jService implements BlockchainService {

    @Getter
    private String nodeName;

    @Getter
    @Setter
    private Web3j web3j;
    private ContractEventDetailsFactory eventDetailsFactory;
    private EventBlockManagementService blockManagement;
    private Lock lock = new ReentrantLock();

    private BlockSubscriptionStrategy blockSubscriptionStrategy;

    public Web3jService(String nodeName,
                        Web3j web3j,
                        ContractEventDetailsFactory eventDetailsFactory,
                        EventBlockManagementService blockManagement,
                        BlockSubscriptionStrategy blockSubscriptionStrategy) {
        this.nodeName = nodeName;
        this.web3j = web3j;
        this.eventDetailsFactory = eventDetailsFactory;
        this.blockManagement = blockManagement;
        this.blockSubscriptionStrategy = blockSubscriptionStrategy;

        connect();
    }

    /**
     * {inheritDoc}
     */
    @Override
    public void addBlockListener(BlockListener blockListener) {
        blockSubscriptionStrategy.addBlockListener(blockListener);
    }

    /**
     * {inheritDoc}
     */
    @Override
    public void removeBlockListener(BlockListener blockListener) {
        blockSubscriptionStrategy.removeBlockListener(blockListener);
    }

    /**
     * {inheritDoc}
     */
    @Override
    public Subscription registerEventListener(
            ContractEventFilter eventFilter, ContractEventListener eventListener) {
        log.debug("Registering event filter for event: {}", eventFilter.getId());
        final ContractEventSpecification eventSpec = eventFilter.getEventSpecification();

        EthFilter ethFilter = new EthFilter(
                new DefaultBlockParameterNumber(getStartBlockForEventFilter(eventFilter)),
                DefaultBlockParameterName.LATEST, eventFilter.getContractAddress());

        if (eventFilter.getEventSpecification() != null) {
            ethFilter = ethFilter.addSingleTopic(Web3jUtil.getSignature(eventSpec));
        }

        final Observable<Log> observable = web3j.ethLogObservable(ethFilter);

        final Subscription sub = observable.subscribe(theLog -> {
            lock.lock();

            try {
                log.debug("Dispatching log: {}", theLog);
                eventListener.onEvent(
                        eventDetailsFactory.createEventDetails(eventFilter, theLog));
            } finally {
                lock.unlock();
            }
        });

        return sub;
    }

    /**
     * {inheritDoc}
     */
    @Override
    public void reconnect() {
        log.info("Reconnecting...");
        try {
            blockSubscriptionStrategy.unsubscribe();
        } catch (FilterException e) {
            log.warn("Unable to unregister block subscription.  " +
                    "This is probably because the node has restarted or we're in websocket mode");
        }
        connect();
    }

    /**
     * {inheritDoc}
     */
    @Override
    public String getClientVersion() {
        try {
            final Web3ClientVersion web3ClientVersion = web3j.web3ClientVersion().send();
            return web3ClientVersion.getWeb3ClientVersion();
        } catch (IOException e) {
            throw new BlockchainException("Error when obtaining client version", e);
        }
    }

    /**
     * {inheritDoc}
     */
    @Override
    public TransactionReceipt getTransactionReceipt(String txId) {
        try {
            final EthGetTransactionReceipt response = web3j.ethGetTransactionReceipt(txId).send();

            return response
                    .getTransactionReceipt()
                    .map(receipt -> new Web3jTransactionReceipt(receipt))
                    .orElse(null);
        } catch (IOException e) {
            throw new BlockchainException("Unable to connect to the ethereum client", e);
        }
    }

    /**
     * {inheritDoc}
     */
    @Override
    public BigInteger getCurrentBlockNumber() {
        try {
            final EthBlockNumber ethBlockNumber = web3j.ethBlockNumber().send();

            return ethBlockNumber.getBlockNumber();
        } catch (IOException e) {
            throw new BlockchainException("Error when obtaining the current block number", e);
        }
    }

    @Override
    public boolean isConnected() {
        return blockSubscriptionStrategy != null && blockSubscriptionStrategy.isSubscribed();
    }

    @PreDestroy
    private void unregisterBlockSubscription() {
        blockSubscriptionStrategy.unsubscribe();
    }

    private void connect() {
        log.info("Subscribing to block events");
        blockSubscriptionStrategy.subscribe();
    }

    private BigInteger getStartBlockForEventFilter(ContractEventFilter filter) {
        return blockManagement.getLatestBlockForEvent(filter);
    }
}
