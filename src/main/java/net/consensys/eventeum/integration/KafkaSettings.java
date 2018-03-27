package net.consensys.eventeum.integration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * An encapsulation of Kafka related properties.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
@Data
@Profile("default")
public class KafkaSettings {

    @Value("${kafka.bootstrap.addresses}")
    private String bootstrapAddresses;

    @Value("${kafka.topic.contractEvents}")
    private String contractEventsTopic;

    @Value("${kafka.topic.blockEvents}")
    private String blockEventsTopic;

    @Value("${kafka.topic.filterEvents}")
    private String filterEventsTopic;

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
