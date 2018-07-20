package net.consensys.eventeum.config;

import net.consensys.eventeum.annotation.ConditionalOnKafkaRequired;
import net.consensys.eventeum.dto.message.EventeumMessage;
import net.consensys.eventeum.integration.KafkaSettings;
import net.consensys.kafkadl.EnableKafkaDeadLetter;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring configuration for Kafka related beans.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Configuration("eventeumKafkaConfiguration")
@EnableKafka
@ConditionalOnKafkaRequired
public class KafkaConfiguration {

    @Autowired
    private KafkaSettings settings;

    @Bean
    public KafkaAdmin eventeumAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, settings.getBootstrapAddresses());
        return new KafkaAdmin(configs);
    }

    @Bean
    public ProducerFactory<String, EventeumMessage> eventeumProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, settings.getBootstrapAddresses());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ConsumerFactory<String, EventeumMessage> eventeumConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, settings.getBootstrapAddresses());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, settings.getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(props, null, new JsonDeserializer<>(EventeumMessage.class));
    }

    @Bean
    public ConsumerFactory<Object, Object> defaultConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, settings.getBootstrapAddresses());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, settings.getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(props, null, new JsonDeserializer<>(Object.class));
    }

    @Bean
    public KafkaTemplate<String, EventeumMessage> eventeumKafkaTemplate() {
        return new KafkaTemplate<>(eventeumProducerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventeumMessage> eventeumKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, EventeumMessage> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(eventeumConsumerFactory());
        factory.setConcurrency(1);
        return factory;
    }

    @Bean
    public NewTopic blockEventsTopic(KafkaSettings kafkaSettings) {
        return new NewTopic(kafkaSettings.getBlockEventsTopic(), 3, Short.parseShort("1"));
    }

    @Bean
    public NewTopic contractEventsTopic(KafkaSettings kafkaSettings) {
        return new NewTopic(kafkaSettings.getContractEventsTopic(), 3, Short.parseShort("1"));
    }

    @Bean
    public NewTopic filterEventsTopic(KafkaSettings kafkaSettings) {
        return new NewTopic(kafkaSettings.getFilterEventsTopic(), 3, Short.parseShort("1"));
    }
}
