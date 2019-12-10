package net.consensys.eventeum.integration.consumer;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.consensys.eventeum.dto.message.EventeumMessage;
import net.consensys.eventeum.service.SubscriptionService;
import net.consensys.eventeum.service.TransactionMonitoringService;

public class HazelCastEventConsumer implements EventeumInternalEventConsumer {
  private static final Logger logger = LoggerFactory.getLogger(BaseEventeumEventConsumer.class);
  EventeumInternalEventConsumer consumer;

  public HazelCastEventConsumer(SubscriptionService subscriptionService, TransactionMonitoringService transactionMonitoringService, HazelcastInstance hazelcastInstance) {
    ITopic<EventeumMessage<?>> topic = hazelcastInstance.getTopic("eventeumInternalEvents");
    consumer = new BaseEventeumEventConsumer(subscriptionService, transactionMonitoringService);
    topic.addMessageListener(new InternalEventeumEventsListener(consumer));
  }

  @Override
  public void onMessage(EventeumMessage<?> message) {
    // Do nothing
  }

  private static class InternalEventeumEventsListener implements MessageListener<EventeumMessage<?>> {
    EventeumInternalEventConsumer consumer;

    InternalEventeumEventsListener(EventeumInternalEventConsumer consumer) {
      this.consumer = consumer;
    }

    public void onMessage(Message<EventeumMessage<?>> m) {
	    logger.info("Received: " + m.getMessageObject());
    	this.consumer.onMessage(m.getMessageObject());
    }
  }
}
