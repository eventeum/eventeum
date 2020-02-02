package io.keyko.monitoring.agent.server.eventeumserver.integrationtest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.integration.mixin.SimplePageImpl;
import io.keyko.monitoring.agent.core.model.LatestBlock;
import org.springframework.data.domain.Page;
import io.keyko.monitoring.agent.core.utils.JSON;
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
