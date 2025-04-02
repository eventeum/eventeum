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

package net.consensys.eventeum.chain.service.block;

import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.settings.NodeSettings;
import net.consensys.eventeum.chain.util.Web3jUtil;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.service.EventStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * The default implementation of an EventBlockManagementService, which "Manages the latest block
 * that has been seen to a specific event specification."
 *
 * <p>This implementation stores the latest blocks for each event filter in memory, but delegates to
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

  private NodeSettings nodeSettings;

  @Autowired
  public DefaultEventBlockManagementService(
      @Lazy ChainServicesContainer chainServicesContainer,
      EventStoreService eventStoreService,
      NodeSettings nodeSettings) {
    this.chainServicesContainer = chainServicesContainer;
    this.eventStoreService = eventStoreService;
    this.nodeSettings = nodeSettings;
  }

  /** {@inheritDoc} */
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

  /** {@inheritDoc} */
  @Override
  public BigInteger getLatestBlockForEvent(ContractEventFilter eventFilter) {
    final BigInteger currentBlockNumber =
        chainServicesContainer
            .getNodeServices(eventFilter.getNode())
            .getBlockchainService()
            .getCurrentBlockNumber();
    final BigInteger maxUnsyncedBlocksForFilter =
        nodeSettings.getNode(eventFilter.getNode()).getMaxBlocksToSync();
    final String eventSignature = Web3jUtil.getSignature(eventFilter.getEventSpecification());
    final AbstractMap<String, BigInteger> events =
        latestBlocks.get(eventFilter.getContractAddress());

    if (events != null) {
      final BigInteger latestBlockNumber = events.get(eventSignature);

      if (latestBlockNumber != null) {

        BigInteger cappedBlockNumber = BigInteger.valueOf(0);
        log.info("currentBlockNumber in event getLatestBlockForEvent: {}", currentBlockNumber);
        log.info("cappedBlockNumber in event getLatestBlockForEvent: {}", cappedBlockNumber);
        if (currentBlockNumber.subtract(latestBlockNumber).compareTo(maxUnsyncedBlocksForFilter)
            == 1) {
          cappedBlockNumber = currentBlockNumber.subtract(maxUnsyncedBlocksForFilter);
          log.info(
              "{} :Max Unsynced Blocks gap reached ´{} to {} . Applied {}. Max {}",
              eventFilter.getId(),
              latestBlockNumber,
              cappedBlockNumber,
              maxUnsyncedBlocksForFilter);
          return cappedBlockNumber;
        } else {
          log.debug(
              "Block number for event> {} found in memory, starting at blockNumber: {}",
              eventFilter.getId(),
              latestBlockNumber.add(BigInteger.ONE));

          return latestBlockNumber.add(BigInteger.ONE);
        }
      }
    }

    final Optional<ContractEventDetails> contractEvent =
        eventStoreService.getLatestContractEvent(eventSignature, eventFilter.getContractAddress());

    if (contractEvent.isPresent()) {
      BigInteger blockNumber = contractEvent.get().getBlockNumber();

      BigInteger limitedBlockNumber = BigInteger.valueOf(0);
      if (currentBlockNumber.subtract(blockNumber).compareTo(maxUnsyncedBlocksForFilter) == 1) {
        limitedBlockNumber = currentBlockNumber.subtract(maxUnsyncedBlocksForFilter);
        log.info("limitedBlockNumber in contract getLatestBlockForEvent: {}", limitedBlockNumber);
        log.info("blockNumber in event getLatestBlockForEvent: {}", blockNumber);
        log.info(
            "{} :Max Unsynced Blocks gap reached ´{} to {} . Applied {}. Max {}",
            eventFilter.getId(),
            currentBlockNumber,
            blockNumber,
            limitedBlockNumber,
            maxUnsyncedBlocksForFilter);
        return limitedBlockNumber;
      } else {

        log.debug(
            "Block number for event {} found in the database, starting at blockNumber: {}",
            eventFilter.getId(),
            blockNumber.add(BigInteger.ONE));

        return blockNumber.add(BigInteger.ONE);
      }
    }

    BigInteger syncStartBlock = nodeSettings.getNode(eventFilter.getNode()).getInitialStartBlock();

    if (syncStartBlock != BigInteger.valueOf(Long.valueOf(NodeSettings.DEFAULT_SYNC_START_BLOCK))) {

      log.debug(
          "Block number for event {}, starting at blockNumber configured with the node special startBlockNumber: {}",
          eventFilter.getId(),
          syncStartBlock);
      return syncStartBlock;
    } else {
      if (eventFilter.getStartBlock() != null) {

        BigInteger blockNumber = eventFilter.getStartBlock();
        BigInteger limitedBlockNumber = BigInteger.valueOf(0);
        if (currentBlockNumber.subtract(blockNumber).compareTo(maxUnsyncedBlocksForFilter) == 1) {
          limitedBlockNumber = currentBlockNumber.subtract(maxUnsyncedBlocksForFilter);
          log.info(
              "limitedBlockNumber in event filter getLatestBlockForEvent: {}", limitedBlockNumber);
          log.info("blockNumber in event filter getLatestBlockForEvent: {}", blockNumber);
          log.info(
              "{} :Max Unsynced Blocks gap reached in event filter ´{} to {} . Applied {}. Max {}",
              eventFilter.getId(),
              currentBlockNumber,
              blockNumber,
              limitedBlockNumber,
              maxUnsyncedBlocksForFilter);
          return limitedBlockNumber;
        } else {
          log.debug(
              "Block number for event {}, starting at blockNumber configured for the event: {}",
              eventFilter.getId(),
              blockNumber);

          return blockNumber;
        }
      }

      final BlockchainService blockchainService =
          chainServicesContainer.getNodeServices(eventFilter.getNode()).getBlockchainService();

      BigInteger blockNumber = blockchainService.getCurrentBlockNumber();

      log.debug(
          "Block number for event {} not found in memory or database, starting at blockNumber: {}",
          eventFilter.getId(),
          blockNumber);

      return blockNumber;
    }
  }
}
