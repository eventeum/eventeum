package net.consensys.eventeum.integration.broadcast.internal;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.*;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import net.consensys.eventeum.utils.JSON;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An EventeumEventBroadcaster that broadcasts the events to Hazelcast
 *
 * @author Fernando Llaca <fernando.llaca@adhara.io>
 */
public class HazelCastEventeumEventBroadcaster implements EventeumEventBroadcaster {

    private static final Logger LOG = LoggerFactory.getLogger(HazelCastEventeumEventBroadcaster.class);
    HazelcastInstance hazelcastInstance;

    public HazelCastEventeumEventBroadcaster(HazelcastInstance hazelcastInstance) {
      this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public void broadcastEventFilterAdded(ContractEventFilter filter) {
        sendMessage(createContractEventFilterAddedMessage(filter));
    }

    @Override
    public void broadcastEventFilterRemoved(ContractEventFilter filter) {
        sendMessage(createContractEventFilterRemovedMessage(filter));
    }

    @Override
    public void broadcastTransactionMonitorAdded(TransactionMonitoringSpec spec) {
        sendMessage(createTransactionMonitorAddedMessage(spec));
    }

    @Override
    public void broadcastTransactionMonitorRemoved(TransactionMonitoringSpec spec) {
        sendMessage(createTransactionMonitorRemovedMessage(spec));
    }

    protected EventeumMessage createContractEventFilterAddedMessage(ContractEventFilter filter) {
        return new ContractEventFilterAdded(filter);
    }

    protected EventeumMessage createContractEventFilterRemovedMessage(ContractEventFilter filter) {
        return new ContractEventFilterRemoved(filter);
    }

    protected EventeumMessage createTransactionMonitorAddedMessage(TransactionMonitoringSpec spec) {
        return new TransactionMonitorAdded(spec);
    }

    protected EventeumMessage createTransactionMonitorRemovedMessage(TransactionMonitoringSpec spec) {
        return new TransactionMonitorRemoved(spec);
    }

    private void sendMessage(EventeumMessage message) {
        LOG.info("Sending message: " + JSON.stringify(message));
        ITopic<EventeumMessage<?>> topic = hazelcastInstance.getTopic("eventeumInternalEvents");
        topic.publish(message);
    }
}

