package net.consensys.eventeumserver.integrationtest;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.message.EventeumMessage;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.annotation.PostConstruct;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:application-test-db-rabbit.properties")
public class RabbitBroadcasterDBEventStoreIT extends BroadcasterSmokeTest {

    public static FixedHostPortGenericContainer rabbitContainer;

    @RabbitListener(bindings = @QueueBinding(
            key = "thisIsRoutingKey.*",
            value = @Queue("ThisIsAEventsQueue"),
            exchange = @Exchange(value = "ThisIsAExchange", type = ExchangeTypes.TOPIC)
    ))
    public void onEvent(EventeumMessage message) {
        if(message.getDetails() instanceof ContractEventDetails){
            onContractEventMessageReceived((ContractEventDetails) message.getDetails());
        } else if(message.getDetails() instanceof BlockDetails){
            onBlockMessageReceived((BlockDetails) message.getDetails());
        } else if(message.getDetails() instanceof TransactionDetails){
            onTransactionMessageReceived((TransactionDetails) message.getDetails());
        }

    }

    @TestConfiguration
    static class RabbitConfig {

        @PostConstruct
        void initRabbit() {
            if (RabbitBroadcasterDBEventStoreIT.rabbitContainer == null) {
                RabbitBroadcasterDBEventStoreIT.rabbitContainer = new FixedHostPortGenericContainer("rabbitmq:3.6.14-management");
                RabbitBroadcasterDBEventStoreIT.rabbitContainer.waitingFor(Wait.forListeningPort());
                RabbitBroadcasterDBEventStoreIT.rabbitContainer.withFixedExposedPort(5672, 5672);
                RabbitBroadcasterDBEventStoreIT.rabbitContainer.start();
            }

        }
    }
}
