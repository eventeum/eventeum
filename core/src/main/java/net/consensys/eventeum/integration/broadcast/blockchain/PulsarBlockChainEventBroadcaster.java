package net.consensys.eventeum.integration.broadcast.blockchain;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

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

@Slf4j
public class PulsarBlockChainEventBroadcaster implements BlockchainEventBroadcaster {
	private final PulsarSettings settings;
	private final ObjectMapper mapper;
	private PulsarClient client;
	private Producer<byte[]> producer;

	public PulsarBlockChainEventBroadcaster(PulsarSettings settings, ObjectMapper mapper) throws PulsarClientException {
		this.settings = settings;
		this.mapper = mapper;

		// TODO support other settings, such as authentication
		client = PulsarClient.builder()
				.serviceUrl(settings.getUrl())
				.build();

		producer = client.newProducer()
				.topic(settings.getTopic())
				.compressionType(CompressionType.LZ4)
				.create();
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
				producer = null;
			}
		}
	}

	@Override
	public void broadcastNewBlock(BlockDetails block) {
		send(block);
	}

	@Override
	public void broadcastContractEvent(ContractEventDetails eventDetails) {
		send(eventDetails);
	}

	private void send(Object data) {
		try {
			producer.send(mapper.writeValueAsBytes(data));
		} catch (PulsarClientException e) {
			// TODO how do we handle this?
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// shouldn't happen
			throw new RuntimeException(e);
		}
	}
}
