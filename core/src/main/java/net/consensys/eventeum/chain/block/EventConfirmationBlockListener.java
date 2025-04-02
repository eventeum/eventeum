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

package net.consensys.eventeum.chain.block;

import java.util.Optional;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.domain.Log;
import net.consensys.eventeum.chain.service.domain.TransactionReceipt;
import net.consensys.eventeum.chain.service.strategy.BlockSubscriptionStrategy;
import net.consensys.eventeum.chain.settings.Node;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class EventConfirmationBlockListener
    extends AbstractConfirmationBlockListener<ContractEventDetails> {

  private static final Logger LOG = LoggerFactory.getLogger(EventConfirmationBlockListener.class);

  private ContractEventDetails contractEvent;
  private BlockchainEventBroadcaster eventBroadcaster;

  public EventConfirmationBlockListener(
      ContractEventDetails contractEvent,
      BlockchainService blockchainService,
      BlockSubscriptionStrategy blockSubscription,
      BlockchainEventBroadcaster eventBroadcaster,
      Node node) {
    super(contractEvent, blockchainService, blockSubscription, node);
    this.contractEvent = contractEvent;
    this.eventBroadcaster = eventBroadcaster;
  }

  @Override
  protected boolean isOrphaned(TransactionReceipt receipt) {
    final Optional<Log> log = getCorrespondingLog(receipt);

    if (log.isPresent()) {

      if (log.get().isRemoved()) {
        LOG.info("Orphan event detected: isRemoved == true");

        return true;
      }

      return super.isOrphaned(receipt);

    } else {

      return true;
    }
  }

  @Override
  protected String getEventIdentifier(ContractEventDetails contractEventDetails) {
    return contractEventDetails.getEventIdentifier();
  }

  @Override
  protected void setStatus(ContractEventDetails contractEventDetails, String status) {
    contractEventDetails.setStatus(ContractEventStatus.valueOf(status));
  }

  @Override
  protected void broadcast(ContractEventDetails contractEventDetails) {
    eventBroadcaster.broadcastContractEvent(contractEvent);
  }

  private Optional<Log> getCorrespondingLog(TransactionReceipt receipt) {
    return receipt.getLogs().stream()
        .filter((log) -> log.getLogIndex().equals(contractEvent.getLogIndex()))
        .findFirst();
  }
}
