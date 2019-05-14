package net.consensys.eventeum.integration.broadcast.blockchain;

import javax.annotation.PreDestroy;

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
