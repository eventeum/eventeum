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

package net.consensys.eventeum.chain.contract;

import java.util.Optional;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A contract event listener that saves the ContractEventDetails to a SaveableEventStore.
 *
 * <p>Only gets registered if a SaveableEventStore exists in the context.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class EventStoreContractEventUpdater implements ContractEventListener {

  private SaveableEventStore saveableEventStore;

  @Autowired
  public EventStoreContractEventUpdater(SaveableEventStore saveableEventStore) {
    this.saveableEventStore = saveableEventStore;
  }

  @Override
  public void onEvent(ContractEventDetails eventDetails) {
    Optional<ContractEventDetails> eventFoundOpt =
        saveableEventStore.getContractEvent(
            eventDetails.getEventSpecificationSignature(),
            eventDetails.getAddress(),
            eventDetails.getBlockHash(),
            eventDetails.getTransactionHash(),
            eventDetails.getLogIndex());
    if (eventFoundOpt.isEmpty()) {
      saveableEventStore.save(eventDetails);
      return;
    }
    ContractEventDetails eventFound = eventFoundOpt.get();
    if (eventFound.getStatus() == ContractEventStatus.UNCONFIRMED
        && eventDetails.getStatus() == ContractEventStatus.CONFIRMED) {
      eventFound.setStatus(ContractEventStatus.CONFIRMED);
      saveableEventStore.save(eventFound);
    }
  }
}
