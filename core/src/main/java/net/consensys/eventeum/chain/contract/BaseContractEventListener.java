package net.consensys.eventeum.chain.contract;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.integration.eventstore.EventStore;

public abstract class BaseContractEventListener implements ContractEventListener {

  protected EventStore eventStore;

  public BaseContractEventListener(EventStore eventStore) {
    this.eventStore = eventStore;
  }

  protected boolean isExistingEvent(ContractEventDetails eventDetails) {
    return eventStore
        .getContractEvent(
            eventDetails.getEventSpecificationSignature(),
            eventDetails.getAddress(),
            eventDetails.getBlockHash(),
            eventDetails.getTransactionHash(),
            eventDetails.getLogIndex())
        .isEmpty();
  }
}
