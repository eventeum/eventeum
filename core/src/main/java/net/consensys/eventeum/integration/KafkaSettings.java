package net.consensys.eventeum.integration;

import lombok.Data;
import net.consensys.eventeum.annotation.ConditionalOnKafkaRequired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * An encapsulation of Kafka related properties.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component("eventeumKafkaSettings")
@ConditionalOnKafkaRequired
@Data
public class KafkaSettings {

    @Value("${kafka.bootstrap.addresses}")
    private String bootstrapAddresses;

    @Value("${kafka.endpoint.identification.algorithm:}")
    private String endpointIdentificationAlgorithm;

    @Value("${kafka.request.timeout.msConfig:20000}")
    private Integer requestTimeoutMsConfig;

    @Value("${kafka.retries:10}")
    private Integer retries;

    @Value("${kafka.retry.backoff.msConfig:500}")
    private Integer retryBackoffMsConfig;

    @Value("${kafka.sasl.mechanism:}")
    private String saslMechanism;

    @Value("${kafka.security.protocol:}")
    private String securityProtocol;

    @Value("${kafka.topic.contractEvents}")
    private String contractEventsTopic;

    @Value("${kafka.topic.blockEvents}")
    private String blockEventsTopic;

    @Value("${kafka.topic.filterEvents}")
    private String filterEventsTopic;

    @Value("${kafka.sasl.username:}")
    private String username;

    @Value("${kafka.sasl.password:}")
    private String password;

    private String groupId;

    public KafkaSettings(@Value("${server.port}") String port) {
        //Generate a random groupId as we want to receive all filter event messages in all instances of service
        initGroupId(port);
    }

    private void initGroupId(String port) {
        try {
            final String hostAddress = InetAddress.getLocalHost().getHostAddress();
            groupId = hostAddress + ":" + port;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
