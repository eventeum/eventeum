package net.consensys.eventeumserver.integrationtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.EventeumMessage;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.integration.KafkaSettings;
import net.consensys.eventeum.utils.JSON;
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
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BaseKafkaIntegrationTest extends BaseIntegrationTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private List<EventeumMessage<ContractEventFilter>> broadcastFiltersEventMessages = new ArrayList<>();

    @Autowired
    private KafkaSettings kafkaSettings;

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, 3);

    private KafkaMessageListenerContainer<String, String> testContainer;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        // set up the Kafka consumer properties
        final Map<String, Object> consumerProperties =
                KafkaTestUtils.consumerProps(generateTestGroupId(), "false", embeddedKafka);

        // create a Kafka consumer factory
        DefaultKafkaConsumerFactory<String, String> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(), new StringDeserializer());

        // set the topic that needs to be consumed
        ContainerProperties containerProperties = new ContainerProperties(kafkaSettings.getContractEventsTopic(),
                kafkaSettings.getFilterEventsTopic(), kafkaSettings.getBlockEventsTopic(), kafkaSettings.getTransactionEventsTopic());

        // create a Kafka MessageListenerContainer
        testContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        // setup a Kafka message listener
        testContainer.setupMessageListener(new MessageListener<String, String>() {
            @Override
            public void onMessage(ConsumerRecord<String, String> record) {
                System.out.println("Received message: " + JSON.stringify(record.value()));
                try {
                    if (record.topic().equals(kafkaSettings.getContractEventsTopic())) {
                        final EventeumMessage<ContractEventDetails> message =
                                objectMapper.readValue(record.value(), EventeumMessage.class);

                        getBroadcastContractEvents().add(message.getDetails());
                    }

                    if (record.topic().equals(kafkaSettings.getFilterEventsTopic())) {
                        final EventeumMessage<ContractEventFilter> message =
                                objectMapper.readValue(record.value(), EventeumMessage.class);

                        getBroadcastFilterEventMessages().add(message);
                    }

                    if (record.topic().equals(kafkaSettings.getBlockEventsTopic())) {
                        final EventeumMessage<BlockDetails> message =
                                objectMapper.readValue(record.value(), EventeumMessage.class);

                        getBroadcastBlockMessages().add(message.getDetails());
                    }

                    if (record.topic().equals(kafkaSettings.getTransactionEventsTopic())) {
                        final EventeumMessage<TransactionDetails> message =
                                objectMapper.readValue(record.value(), EventeumMessage.class);

                        getBroadcastTransactionMessages().add(message.getDetails());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // start the container and underlying message listener
        testContainer.start();

        ContainerTestUtils.waitForAssignment(testContainer,
                embeddedKafka.getPartitionsPerTopic() * testContainer.getContainerProperties().getTopics().length);

        clearMessages();
    }

    @After
    public void tearDown() {
        // stop the container
        testContainer.stop();
    }

    public List<EventeumMessage<ContractEventFilter>> getBroadcastFilterEventMessages() {
        return broadcastFiltersEventMessages;
    }

    protected void clearMessages() {
        super.clearMessages();
        broadcastFiltersEventMessages.clear();
    }

    private String generateTestGroupId() {
        return "testGroup-" + UUID.randomUUID().toString();
    }

}
