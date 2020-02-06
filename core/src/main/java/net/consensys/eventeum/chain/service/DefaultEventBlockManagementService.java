/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.consensys.eventeum.chain.service;

import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.container.NodeServices;
import net.consensys.eventeum.chain.settings.Node;
import net.consensys.eventeum.chain.util.Web3jUtil;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.service.EventStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.Optional;
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
@Slf4j
@Component
public class DefaultEventBlockManagementService implements EventBlockManagementService {


    @Value("#{new Boolean('${ethereum.syncFromLatest}')}")
    private Boolean syncFromLatest = false;

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
        final NodeServices nodeServices = chainServicesContainer.getNodeServices(eventFilter.getNode());
        final BlockchainService blockchainService = nodeServices.getBlockchainService();

        BigInteger currentBlockNumber =  blockchainService.getCurrentBlockNumber();

        if (this.syncFromLatest) {

            log.debug("Using SyncFromLatest, starting at blockNumber: {}", eventFilter.getId(), currentBlockNumber);

            return currentBlockNumber;
        }

        BigInteger latestBlockNumber = calculateLatestBlockForEvent(eventFilter, currentBlockNumber);

        return getCappedBlockNumber(latestBlockNumber, currentBlockNumber, nodeServices.getNode());
    }

    private BigInteger calculateLatestBlockForEvent(ContractEventFilter eventFilter, BigInteger currentBlockNumber) {
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

        log.debug("Block number for event {} not found in memory or database, starting at blockNumber: {}", eventFilter.getId(), currentBlockNumber);

        return currentBlockNumber;
    }

    protected BigInteger getCappedBlockNumber(BigInteger latestBlockNumber, BigInteger currentBlockNumber, Node node) {

        try {
            BigInteger maxUnsyncedBlocksForFilter = node.getMaxUnsyncedBlocksForFilter();


            BigInteger cappedBlockNumber = BigInteger.valueOf(0);

            if (!BigInteger.valueOf(0).equals(maxUnsyncedBlocksForFilter) && currentBlockNumber.subtract(latestBlockNumber).compareTo(maxUnsyncedBlocksForFilter) == 1) {
                cappedBlockNumber = currentBlockNumber.subtract(maxUnsyncedBlocksForFilter);
                log.info("BLOCK: Max Unsynced Blocks gap reached ´{} to {} . Applied {}. Max {}", latestBlockNumber, currentBlockNumber, cappedBlockNumber, maxUnsyncedBlocksForFilter);
                return cappedBlockNumber;
            }
        }
        catch(Exception e){
            log.error("Could not get current block to possibly cap range",e);
        }

        return latestBlockNumber;
    }
}
