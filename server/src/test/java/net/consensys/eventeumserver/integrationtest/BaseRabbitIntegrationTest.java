package net.consensys.eventeumserver.integrationtest;

import junit.framework.TestCase;
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

    private List<BlockDetails> broadcastBlockMessages = new ArrayList<>();

    public List<EventeumMessage<ContractEventFilter>> getBroadcastFilterEventMessages() {
        return broadcastFiltersEventMessages;
    }

    public List<BlockDetails> getBroadcastBlockMessages() {
        return broadcastBlockMessages;
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
            broadcastBlockMessages.add((BlockDetails) message.getDetails());
        }

    }

    protected void waitForBlockMessages(int expectedBlockMessages) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final long startTime = System.currentTimeMillis();
        while(true) {
            if (broadcastBlockMessages.size() >= expectedBlockMessages) {
                break;
            }

            if (System.currentTimeMillis() > startTime + 20000) {
                final StringBuilder builder = new StringBuilder("Failed to receive all expected messages");
                builder.append("\n");
                builder.append("Expected block messages: " + expectedBlockMessages);
                builder.append(", received: " + broadcastBlockMessages.size());

                TestCase.fail(builder.toString());
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
