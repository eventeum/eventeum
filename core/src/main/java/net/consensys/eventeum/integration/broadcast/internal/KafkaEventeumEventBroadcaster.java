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

package net.consensys.eventeum.integration.broadcast.internal;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.*;
import net.consensys.eventeum.integration.KafkaSettings;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import net.consensys.eventeum.utils.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * An EventeumEventBroadcaster that broadcasts the events to a Kafka queue.
 *
 * The topic name can be configured via the kafka.topic.eventeumEvents property.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class KafkaEventeumEventBroadcaster implements EventeumEventBroadcaster {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaEventeumEventBroadcaster.class);

    private KafkaTemplate<String, EventeumMessage> kafkaTemplate;

    private KafkaSettings kafkaSettings;

    public KafkaEventeumEventBroadcaster(KafkaTemplate<String, EventeumMessage> kafkaTemplate,
                                         KafkaSettings kafkaSettings) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaSettings = kafkaSettings;
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
        kafkaTemplate.send(kafkaSettings.getEventeumEventsTopic(), message.getId(), message);
    }
}
