package net.consensys.eventeum.chain.service;

import net.consensys.eventeum.chain.util.Web3jUtil;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.service.EventStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The default implementation of an EventBlockManagementService, which "Manages the latest block
 * that has been seen to a specific event specification."
 *
 * This implementation stores the latest blocks for each event filter in memory, but delegates to
 * the event store if an entry is not found in memory.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
public class DefaultEventBlockManagementService implements EventBlockManagementService {

    private AbstractMap<String, BigInteger> latestBlocks = new ConcurrentHashMap<>();

    private BlockchainService blockchainService;

    private EventStoreService eventStoreService;

    @Autowired
    public DefaultEventBlockManagementService(@Lazy BlockchainService blockchainService,
                                              EventStoreService eventStoreService) {
        this.blockchainService = blockchainService;
        this.eventStoreService = eventStoreService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateLatestBlock(String eventSpecHash, BigInteger blockNumber) {
        final BigInteger currentLatest = latestBlocks.get(eventSpecHash);

        if (currentLatest == null || blockNumber.compareTo(currentLatest) > 0) {
            latestBlocks.put(eventSpecHash, blockNumber);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigInteger getLatestBlockForEvent(ContractEventSpecification eventSpec) {
        final String eventSignature = Web3jUtil.getSignature(eventSpec);
        final BigInteger latestBlockNumber = latestBlocks.get(eventSignature);

        if (latestBlockNumber != null) {
            return latestBlockNumber;
        }

        final ContractEventDetails contractEvent = eventStoreService.getLatestContractEvent(eventSignature);

        if (contractEvent != null) {
            return contractEvent.getBlockNumber();
        }

        return blockchainService.getCurrentBlockNumber();
    }
}
