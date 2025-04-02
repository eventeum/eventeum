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
package net.consensys.eventeumserver.integrationtest.container;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

public class PulsarContainer<SelfT extends PulsarContainer<SelfT>> extends GenericContainer<SelfT> {

  private static final Logger log = LoggerFactory.getLogger(PulsarContainer.class);

  public static final int BROKER_PORT = 6650;
  public static final int BROKER_HTTP_PORT = 8080;
  public static final String METRICS_ENDPOINT = "/metrics";

  private static final String IMAGE_NAME = "apachepulsar/pulsar:2.3.1";

  public PulsarContainer() {
    super(IMAGE_NAME);
  }

  @Override
  protected void configure() {
    super.configure();
    this.addExposedPorts(BROKER_PORT, BROKER_HTTP_PORT);
  }

  @Override
  public void start() {
    this.waitStrategy =
        new HttpWaitStrategy()
            .forPort(BROKER_HTTP_PORT)
            .forStatusCode(200)
            .forPath(METRICS_ENDPOINT)
            .withStartupTimeout(Duration.of(60, SECONDS));

    this.withCommand("/bin/bash", "-c", "/pulsar/bin/pulsar standalone");

    this.withCreateContainerCmdModifier(
        createContainerCmd -> {
          createContainerCmd.withHostName("standalone");
        });

    super.start();
    log.info("Pulsar Service Started");
  }

  public String getPlainTextServiceUrl() {
    return "pulsar://" + getContainerIpAddress() + ":" + getMappedPort(BROKER_PORT);
  }

  public String getHttpServiceUrl() {
    return "http://" + getContainerIpAddress() + ":" + getMappedPort(BROKER_HTTP_PORT);
  }
}
