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

import com.github.tomakehurst.wiremock.WireMockServer;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.integration.mixin.SimplePageImpl;
import net.consensys.eventeum.model.LatestBlock;
import org.springframework.data.domain.Page;
import net.consensys.eventeum.utils.JSON;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.math.BigInteger;
import java.util.Arrays;

public class StubEventStoreService {

    private static WireMockServer wireMockServer;

    static void start() {
        wireMockServer = new WireMockServer(wireMockConfig().port(8081));
        wireMockServer.start();

        final ContractEventDetails dummyContractEventDetails = new ContractEventDetails();
        dummyContractEventDetails.setBlockNumber(BigInteger.TEN);

        final LatestBlock dummyLatestBlock = new LatestBlock();
        dummyLatestBlock.setNumber(BigInteger.ZERO);

        final Page<ContractEventDetails> dummyPage = new SimplePageImpl<>(Arrays.asList(dummyContractEventDetails), 1, 1, 1);

        wireMockServer.addStubMapping(get(urlPathEqualTo("/api/rest/v1/event"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                        .withBody(JSON.stringify(dummyPage))).build());

        wireMockServer.addStubMapping(get(urlPathEqualTo("/api/rest/v1/latestblock"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                        .withBody(JSON.stringify(dummyLatestBlock))).build());
    }

    static void stop() {
        wireMockServer.stop();
    }
}
