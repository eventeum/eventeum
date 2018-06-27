package net.consensys.eventeum.integrationtest;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.Message;
import net.consensys.eventeum.utils.JSON;
import org.junit.Before;
import org.junit.ClassRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.test.rule.KafkaEmbedded;

import java.util.ArrayList;
import java.util.List;

public class BaseKafkaIntegrationTest extends BaseIntegrationTest {

    private List<Message<ContractEventFilter>> broadcastFiltersEventMessages = new ArrayList<>();

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true);

    @Before
    public void setUp() throws Exception {
        super.setUp();

        Thread.sleep(15000);

        clearMessages();

//        // wait until the partitions are assigned
//        for (MessageListenerContainer messageListenerContainer : kafkaListenerEndpointRegistry
//                .getListenerContainers()) {
//            ContainerTestUtils.waitForAssignment(messageListenerContainer, 3);
//        }
    }

    public List<Message<ContractEventFilter>> getBroadcastFilterEventMessages() {
        return broadcastFiltersEventMessages;
    }

    protected void clearMessages() {
        super.clearMessages();
        broadcastFiltersEventMessages.clear();
    }

    @KafkaListener(topics = "#{kafkaSettings.filterEventsTopic}", groupId="testGroup")
    public void onFilterEventMessage(Message<ContractEventFilter> message) {
        broadcastFiltersEventMessages.add(message);
    }


    @KafkaListener(topics = "#{kafkaSettings.contractEventsTopic}", groupId="testGroup")
    public void onContractEventMessage(Message<ContractEventDetails> contractEventMessage) {
        getBroadcastContractEvents().add(contractEventMessage.getDetails());
    }
}
