package io.keyko.monitoring.agent.core.config;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.keyko.monitoring.agent.core.annotation.ConditionalOnKafkaRequired;
import io.keyko.monitoring.agent.core.integration.KafkaSettings;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ProducerFactory<String, GenericRecord> eventeumProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, settings.getBootstrapAddresses());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, settings.getRequestTimeoutMsConfig());
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, settings.getRetryBackoffMsConfig());
        configProps.put("retries", settings.getRetries());
        configProps.put("schema.registry.url", settings.getSchemaRegistryUrl());

        if ("PLAINTEXT".equals(settings.getSecurityProtocol())) {
            configurePlaintextSecurityProtocol(configProps);
        }
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ConsumerFactory<String, GenericRecord> eventeumConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, settings.getBootstrapAddresses());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, settings.getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, settings.getRequestTimeoutMsConfig());
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, settings.getRetryBackoffMsConfig());
        props.put("schema.registry.url", settings.getSchemaRegistryUrl());

        if ("PLAINTEXT".equals(settings.getSecurityProtocol())) {
            configurePlaintextSecurityProtocol(props);
        }
        return new DefaultKafkaConsumerFactory<>(props);
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
    public KafkaTemplate<String, GenericRecord> eventeumKafkaTemplate() {
        return new KafkaTemplate<>(eventeumProducerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, GenericRecord> eventeumKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, GenericRecord> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(eventeumConsumerFactory());
        factory.setConcurrency(1);
        return factory;
    }

    @Bean
    public NewTopic blockEventsTopic(KafkaSettings kafkaSettings) {
        return new NewTopic(kafkaSettings.getBlockEventsTopic(),
                kafkaSettings.getPartitions(), kafkaSettings.getReplicationSets().shortValue());
    }

    @Bean
    public NewTopic contractEventsTopic(KafkaSettings kafkaSettings) {
        return new NewTopic(kafkaSettings.getContractEventsTopic(),
                kafkaSettings.getPartitions(), kafkaSettings.getReplicationSets().shortValue());
    }

    @Bean
    public NewTopic eventeumEventsTopic(KafkaSettings kafkaSettings) {
        return new NewTopic(kafkaSettings.getEventeumEventsTopic(),
                kafkaSettings.getPartitions(), kafkaSettings.getReplicationSets().shortValue());
    }

    @Bean
    public NewTopic transactionEventsTopic(KafkaSettings kafkaSettings) {
        return new NewTopic(kafkaSettings.getTransactionEventsTopic(),
                kafkaSettings.getPartitions(), kafkaSettings.getReplicationSets().shortValue());
    }

    private void configurePlaintextSecurityProtocol(Map<String, Object> configProps) {
        configProps.put("ssl.endpoint.identification.algorithm", settings.getEndpointIdentificationAlgorithm());
        configProps.put("sasl.mechanism", settings.getSaslMechanism());
        configProps.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + settings.getUsername() + "\" password=\"" + settings.getPassword() + "\";");
        configProps.put("security.protocol", settings.getSecurityProtocol());
    }
}