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

package net.consensys.eventeumserver.integrationtest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.io.IOException;
import java.util.List;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import org.springframework.http.HttpStatus;

public class StubHttpConsumer {

  private ObjectMapper objectMapper = new ObjectMapper();

  private WireMockServer wireMockServer;

  private HttpStatus responseStatus;

  public StubHttpConsumer(HttpStatus responseStatus) {
    this.responseStatus = responseStatus;

    wireMockServer = new WireMockServer(wireMockConfig().port(8082));
  }

  public StubHttpConsumer() {
    this(HttpStatus.OK);
  }

  public void start(
      List<ContractEventDetails> broadcastContractEvents, List<BlockDetails> broadcastBlocks) {
    wireMockServer.start();

    wireMockServer.addStubMapping(
        post(urlPathEqualTo("/consumer/block-event"))
            .willReturn(aResponse().withStatus(responseStatus.value()))
            .build());

    wireMockServer.addStubMapping(
        post(urlPathEqualTo("/consumer/contract-event"))
            .willReturn(aResponse().withStatus(responseStatus.value()))
            .build());

    wireMockServer.addMockServiceRequestListener(
        (request, response) -> {
          if (request.getUrl().contains("/consumer/contract-event")) {
            final String body = request.getBodyAsString();

            try {
              broadcastContractEvents.add(objectMapper.readValue(body, ContractEventDetails.class));
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        });

    wireMockServer.addMockServiceRequestListener(
        (request, response) -> {
          if (request.getUrl().contains("/consumer/block-event")) {
            final String body = request.getBodyAsString();

            try {
              broadcastBlocks.add(objectMapper.readValue(body, BlockDetails.class));
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        });
  }

  public void stop() {
    wireMockServer.stop();
  }

  public String getLatestRequestBody() {
    List<ServeEvent> events = wireMockServer.getAllServeEvents();

    return events.get(events.size() - 1).getRequest().getBodyAsString();
  }
}
