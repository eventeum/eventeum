package net.consensys.eventeum.integration.broadcast.blockchain;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.message.BlockEvent;
import net.consensys.eventeum.dto.message.ContractEvent;
import net.consensys.eventeum.dto.message.EventeumMessage;
import net.consensys.eventeum.integration.RabbitSettings;
import net.consensys.eventeum.utils.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * A RabbitBlockChainEventBroadcaster that broadcasts the events to a RabbitMQ exchange.
 *
 * The routing key for each message will defined by the routingKeyPrefix configured,
 * plus filterId for new contract events.
 *
 * The exchange and routingKeyPrefix can be configured via the
 * rabbitmq.exchange and rabbitmq.routingKeyPrefix properties.
 *
 * @author ioBuilders technical team <tech@io.builders>
 */
public class RabbitBlockChainEventBroadcaster implements BlockchainEventBroadcaster {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitBlockChainEventBroadcaster.class);
    private static final String BLOCKEVENT_ROUTING_KEY_SUFIX = "NewBlock";

    private RabbitTemplate rabbitTemplate;
    private RabbitSettings rabbitSettings;

    public RabbitBlockChainEventBroadcaster(RabbitTemplate rabbitTemplate, RabbitSettings rabbitSettings) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitSettings = rabbitSettings;
    }

    @Override
    public void broadcastNewBlock(BlockDetails block) {
        final EventeumMessage<BlockDetails> message = createBlockEventMessage(block);
        rabbitTemplate.convertAndSend(this.rabbitSettings.getExchange(),
                String.format("%s.%s", this.rabbitSettings.getRoutingKeyPrefix(), BLOCKEVENT_ROUTING_KEY_SUFIX),
                message);

        LOG.info(String.format("Sent new block: [%s] to exchange [%s] with routing key [%s.%s]",
                JSON.stringify(message),
                this.rabbitSettings.getExchange(),
                this.rabbitSettings.getRoutingKeyPrefix(),
                BLOCKEVENT_ROUTING_KEY_SUFIX));
    }

    @Override
    public void broadcastContractEvent(ContractEventDetails eventDetails) {
        final EventeumMessage<ContractEventDetails> message = createContractEventMessage(eventDetails);
        rabbitTemplate.convertAndSend(this.rabbitSettings.getExchange(),
                String.format("%s.%s", this.rabbitSettings.getRoutingKeyPrefix(), eventDetails.getFilterId()),
                message);

        LOG.info(String.format("Sent new contract event: [%s] to exchange [%s] with routing key [%s.%s]",
                JSON.stringify(message),
                this.rabbitSettings.getExchange(),
                this.rabbitSettings.getRoutingKeyPrefix(),
                eventDetails.getFilterId()));
    }

    protected EventeumMessage<BlockDetails> createBlockEventMessage(BlockDetails blockDetails) {
        return new BlockEvent(blockDetails);
    }

    protected EventeumMessage<ContractEventDetails> createContractEventMessage(ContractEventDetails contractEventDetails) {
        return new ContractEvent(contractEventDetails);
    }

}
