package net.consensys.eventeum.chain.block.message;

import net.consensys.eventeum.dto.message.MessageDetails;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.integration.eventstore.EventStore;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BroadcastingMessageListener implements MessageListener {

  private EventStore eventStore;
  private BlockchainEventBroadcaster eventBroadcaster;

  @Autowired
  public BroadcastingMessageListener(
      EventStore eventStore, BlockchainEventBroadcaster eventBroadcaster) {
    this.eventStore = eventStore;
    this.eventBroadcaster = eventBroadcaster;
  }

  @Override
  public void onMessage(MessageDetails messageDetails) {
    eventBroadcaster.broadcastMessage(messageDetails);
    if (eventStore instanceof SaveableEventStore) {
      ((SaveableEventStore) eventStore).save(messageDetails);
    }
  }
}
