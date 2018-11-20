package net.consensys.eventeumserver.integrationtest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.integration.mixin.SimplePageImpl;
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

        final Page<ContractEventDetails> dummyPage = new SimplePageImpl<>(Arrays.asList(dummyContractEventDetails), 1, 1, 1);

        wireMockServer.addStubMapping(get(urlPathEqualTo("/api/rest/v1/event"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                        .withBody(JSON.stringify(dummyPage))).build());
    }

    static void stop() {
        wireMockServer.stop();
    }
}
