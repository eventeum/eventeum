package net.consensys.eventeumserver.integrationtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestFailure;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.ContractEvent;
import net.consensys.eventeum.dto.message.EventeumMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class BaseKafkaIntegrationTest extends BaseIntegrationTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private List<EventeumMessage<ContractEventFilter>> broadcastFiltersEventMessages = new ArrayList<>();

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Value("#{eventeumKafkaSettings.contractEventsTopic}")
    private String contractEventsTopic;

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, 3);

    private KafkaMessageListenerContainer<String, String> contractEventsContainer;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        // set up the Kafka consumer properties
        final Map<String, Object> consumerProperties =
                KafkaTestUtils.consumerProps("testGroup", "false", embeddedKafka);

        // create a Kafka consumer factory
        DefaultKafkaConsumerFactory<String, String> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(), new StringDeserializer());

        // set the topic that needs to be consumed
        ContainerProperties containerProperties = new ContainerProperties(contractEventsTopic);

        // create a Kafka MessageListenerContainer
        contractEventsContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        // setup a Kafka message listener
        contractEventsContainer.setupMessageListener(new MessageListener<String, String>() {
            @Override
            public void onMessage(ConsumerRecord<String, String> record) {
                try {
                    final EventeumMessage<ContractEventDetails> message =
                            objectMapper.readValue(record.value(), EventeumMessage.class);

                    getBroadcastContractEvents().add(message.getDetails());
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // start the container and underlying message listener
        contractEventsContainer.start();

//        Thread.sleep(15000);
        ContainerTestUtils.waitForAssignment(contractEventsContainer, embeddedKafka.getPartitionsPerTopic());

        clearMessages();
    }

    @After
    public void tearDown() {
        // stop the container
        contractEventsContainer.stop();
    }

    public List<EventeumMessage<ContractEventFilter>> getBroadcastFilterEventMessages() {
        return broadcastFiltersEventMessages;
    }

    protected void clearMessages() {
        super.clearMessages();
        broadcastFiltersEventMessages.clear();
    }

    @KafkaListener(containerFactory = "eventeumKafkaListenerContainerFactory",
            topics = "#{eventeumKafkaSettings.filterEventsTopic}", groupId="testGroup")
    public void onFilterEventMessage(EventeumMessage<ContractEventFilter> message) {
        broadcastFiltersEventMessages.add(message);
    }
}
