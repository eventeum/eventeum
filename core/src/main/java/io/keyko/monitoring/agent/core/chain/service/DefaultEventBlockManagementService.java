package io.keyko.monitoring.agent.core.chain.service;

import io.keyko.monitoring.agent.core.chain.service.container.ChainServicesContainer;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import lombok.extern.slf4j.Slf4j;
import io.keyko.monitoring.agent.core.chain.util.Web3jUtil;
import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.service.EventStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The default implementation of an EventBlockManagementService, which "Manages the latest block
 * that has been seen to a specific event specification."
 * <p>
 * This implementation stores the latest blocks for each event filter in memory, but delegates to
 * the event store if an entry is not found in memory.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Slf4j
@Component
public class DefaultEventBlockManagementService implements EventBlockManagementService {

    private AbstractMap<String, AbstractMap> latestBlocks = new ConcurrentHashMap<>();

    private ChainServicesContainer chainServicesContainer;

    private EventStoreService eventStoreService;

    @Autowired
    public DefaultEventBlockManagementService(@Lazy ChainServicesContainer chainServicesContainer,
                                              EventStoreService eventStoreService) {
        this.chainServicesContainer = chainServicesContainer;
        this.eventStoreService = eventStoreService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateLatestBlock(String eventSpecHash, BigInteger blockNumber, String address) {
        AbstractMap<String, BigInteger> events = latestBlocks.get(address);

        if (events == null) {
            events = new ConcurrentHashMap<>();
            latestBlocks.put(address, events);
        }

        final BigInteger currentLatest = events.get(eventSpecHash);


        if (currentLatest == null || blockNumber.compareTo(currentLatest) > 0) {
            events.put(eventSpecHash, blockNumber);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigInteger getLatestBlockForEvent(ContractEventFilter eventFilter) {
        final String eventSignature = Web3jUtil.getSignature(eventFilter.getEventSpecification());
        final AbstractMap<String, BigInteger> events = latestBlocks.get(eventFilter.getContractAddress());

        if (events != null) {
            final BigInteger latestBlockNumber = events.get(eventSignature);

            if (latestBlockNumber != null) {
                log.debug("Block number for event {} found in memory, starting at blockNumber: {}", eventFilter.getId(), latestBlockNumber);

                return latestBlockNumber;
            }
        }

        final Optional<ContractEventDetails> contractEvent =
                eventStoreService.getLatestContractEvent(eventSignature, eventFilter.getContractAddress());

        if (contractEvent.isPresent()) {
            BigInteger blockNumber = contractEvent.get().getBlockNumber();

            log.debug("Block number for event {} found in the database, starting at blockNumber: {}", eventFilter.getId(), blockNumber);

            return blockNumber;
        }

        if (eventFilter.getStartBlock() != null) {
            BigInteger blockNumber = eventFilter.getStartBlock();

            log.debug("Block number for event {}, starting at blockNumber configured for the event: {}", eventFilter.getId(), blockNumber);

            return blockNumber;
        }

        final BlockchainService blockchainService =
                chainServicesContainer.getNodeServices(eventFilter.getNode()).getBlockchainService();

        BigInteger blockNumber = blockchainService.getCurrentBlockNumber();

        log.debug("Block number for event {} not found in memory or database, starting at blockNumber: {}", eventFilter.getId(), blockNumber);

        return blockNumber;
    }
}
