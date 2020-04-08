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

package net.consensys.eventeum.integration.broadcast.blockchain;

import javax.annotation.PreDestroy;

import net.consensys.eventeum.dto.transaction.TransactionDetails;
import org.apache.pulsar.client.api.ClientBuilder;
import org.apache.pulsar.client.api.CompressionType;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.integration.PulsarSettings;
import net.consensys.eventeum.integration.PulsarSettings.Authentication;
import net.consensys.eventeum.integration.broadcast.BroadcastException;

@Slf4j
        public class PulsarBlockChainEventBroadcaster implements BlockchainEventBroadcaster {
	private final ObjectMapper mapper;
	private PulsarClient client;
	private Producer<byte[]> blockEventProducer;
	private Producer<byte[]> contractEventProducer;
    private Producer<byte[]> transactionEventProducer;

	public PulsarBlockChainEventBroadcaster(PulsarSettings settings, ObjectMapper mapper) throws PulsarClientException {
		this.mapper = mapper;

		ClientBuilder builder = PulsarClient.builder();

		if (settings.getConfig() != null) {
			builder.loadConf(settings.getConfig());
		}

		Authentication authSettings = settings.getAuthentication();
		if (authSettings != null) {
			builder.authentication(
					authSettings.getPluginClassName(),
					authSettings.getParams());
		}

		client = builder.build();

		blockEventProducer = createProducer(settings.getTopic().getBlockEvents());
		contractEventProducer = createProducer(settings.getTopic().getContractEvents());
        transactionEventProducer = createProducer(settings.getTopic().getTransactionEvents());
	}

	@PreDestroy
	public void destroy() {
		if (client != null) {
			try {
				client.close();
			} catch (PulsarClientException e) {
				log.warn("couldn't close Pulsar client", e);
			} finally {
				client = null;
				blockEventProducer = null;
				contractEventProducer = null;
			}
		}
	}

	@Override
	public void broadcastNewBlock(BlockDetails block) {
		send(block, blockEventProducer);
	}

	@Override
	public void broadcastContractEvent(ContractEventDetails eventDetails) {
		send(eventDetails, contractEventProducer);
	}

    @Override
    public void broadcastTransaction(TransactionDetails transactionDetails) {
        send(transactionDetails, transactionEventProducer);
    }

    protected Producer<byte[]> createProducer(String topic) throws PulsarClientException {
		return client.newProducer()
				.topic(topic)
				.compressionType(CompressionType.LZ4)
				.create();
	}

	private void send(Object data, Producer<byte[]> producer) {
		try {
			producer.send(mapper.writeValueAsBytes(data));
		} catch (PulsarClientException e) {
			throw new BroadcastException("Unable to send message", e);
		} catch (JsonProcessingException e) {
			// shouldn't happen
			throw new RuntimeException(e);
		}
	}

}
