/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.consensys.eventeum.integration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.Data;
import net.consensys.eventeum.annotation.ConditionalOnKafkaRequired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

  @Value("${kafka.topic.eventeumEvents}")
  private String eventeumEventsTopic;

  @Value("${kafka.topic.transactionEvents}")
  private String transactionEventsTopic;

  @Value("${kafka.topic.messageEvents}")
  private String messageEventsTopic;

  @Value("${kafka.topic.partitions:3}")
  private Integer partitions;

  @Value("${kafka.topic.replicationSets:1}")
  private Integer replicationSets;

  @Value("${kafka.sasl.username:}")
  private String username;

  @Value("${kafka.sasl.password:}")
  private String password;

  private String groupId;

  public KafkaSettings(@Value("${server.port}") String port) {
    // Generate a random groupId as we want to receive all filter event messages in all instances of
    // service
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

  public String getEventeumEventsTopic() {
    return eventeumEventsTopic;
  }

  public String getContractEventsTopic() {
    return contractEventsTopic;
  }

  public String getBlockEventsTopic() {
    return blockEventsTopic;
  }

  public String getTransactionEventsTopic() {
    return transactionEventsTopic;
  }

  public String getMessageEventsTopic() {
    return messageEventsTopic;
  }
}
