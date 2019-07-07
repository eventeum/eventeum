package net.consensys.eventeumserver.integrationtest;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.EventeumMessage;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;


import java.util.ArrayList;
import java.util.List;

public class BaseRabbitIntegrationTest extends BaseIntegrationTest {

    private List<EventeumMessage<ContractEventFilter>> broadcastFiltersEventMessages = new ArrayList<>();

    public List<EventeumMessage<ContractEventFilter>> getBroadcastFilterEventMessages() {
        return broadcastFiltersEventMessages;
    }

    protected void clearMessages() {
        super.clearMessages();
        broadcastFiltersEventMessages.clear();
    }

    @RabbitListener(bindings = @QueueBinding(
            key = "thisIsRoutingKey.*",
            value = @Queue("ThisIsAEventsQueue"),
            exchange = @Exchange(value = "ThisIsAExchange", type = ExchangeTypes.TOPIC)
    ))
    public void onEvent(EventeumMessage message) {
        if(message.getDetails() instanceof ContractEventDetails){
            getBroadcastContractEvents().add((ContractEventDetails) message.getDetails());
        }
        else if(message.getDetails() instanceof BlockDetails){
            getBroadcastBlockMessages().add((BlockDetails) message.getDetails());
        }

    }
}
