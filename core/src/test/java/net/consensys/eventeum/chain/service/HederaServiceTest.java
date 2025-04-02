package net.consensys.eventeum.chain.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import net.consensys.eventeum.chain.factory.ContractEventDetailsFactory;
import net.consensys.eventeum.chain.service.block.EventBlockManagementService;
import net.consensys.eventeum.chain.service.domain.io.*;
import net.consensys.eventeum.chain.settings.Node;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.dto.event.filter.ParameterDefinition;
import net.consensys.eventeum.dto.event.filter.ParameterType;
import net.consensys.eventeum.service.EventStoreService;
import net.consensys.eventeum.service.SubscriptionService;
import net.consensys.eventeum.service.exception.NotFoundException;
import net.consensys.eventeum.testutils.DummyAsyncTaskService;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.internal.util.Assert;

class HederaServiceTest {
  private static final String NODE_URL = "http://test.test";

  private static final String NODE_NAME = "test";

  private ContractEventDetailsFactory contractEventDetailsFactory;

  private ObjectMapper objectMapper;

  private HederaService hederaService;

  private Call remoteCall;

  private EventBlockManagementService mockBlockManagement;

  @BeforeEach
  public void init() {
    final EventStoreService eventStoreService = mock(EventStoreService.class);
    contractEventDetailsFactory = mock(ContractEventDetailsFactory.class);
    final OkHttpClient okHttpClient = mock(OkHttpClient.class);
    ScheduledExecutorService executorService = mock(ScheduledExecutorService.class);
    final SubscriptionService mockSubscriptionService = mock(SubscriptionService.class);
    final ModelMapper modelMapper = new ModelMapper();
    objectMapper = new ObjectMapper();
    Node node = new Node();
    node.setName(NODE_NAME);
    node.setUrl(NODE_URL);
    this.hederaService =
        new HederaService(
            contractEventDetailsFactory,
            eventStoreService,
            objectMapper,
            node,
            executorService,
            modelMapper,
            okHttpClient,
            mockBlockManagement,
            new DummyAsyncTaskService(),
            mockSubscriptionService);

    remoteCall = mock(Call.class);
    when(okHttpClient.newCall(any())).thenReturn(remoteCall);
  }

  @Test
  public void getContractResults() throws IOException, NotFoundException {
    ContractResultsResponse contractResultsResponse = new ContractResultsResponse();
    ContractResultResponse contractResultResponse = new ContractResultResponse();
    BigInteger blockNumber = BigInteger.valueOf(5670700);
    contractResultResponse.setBlockNumber(blockNumber.toString());
    contractResultsResponse.setResults(Collections.singletonList(contractResultResponse));

    final Response httpResponse =
        new Response.Builder()
            .request(new Request.Builder().url("https://url.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("")
            .body(
                ResponseBody.create(
                    this.objectMapper.writeValueAsString(contractResultsResponse),
                    MediaType.parse("application/json")))
            .build();

    when(remoteCall.execute()).thenReturn(httpResponse);

    ContractResultsResponse response = this.hederaService.getContractResultsByBlock(blockNumber);
    Assert.notNull(response);
    Assert.isTrue(response.getResults().size() == 1);
    Assert.isTrue(response.getResults().get(0).equals(contractResultResponse));
  }

  @Test
  public void getContractResult() throws IOException, NotFoundException {
    ContractResultResponse contractResultResponse = new ContractResultResponse();
    contractResultResponse.setContractId("0.0.1");

    final Response httpResponse =
        new Response.Builder()
            .request(new Request.Builder().url("https://url.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("")
            .body(
                ResponseBody.create(
                    this.objectMapper.writeValueAsString(contractResultResponse),
                    MediaType.parse("application/json")))
            .build();

    when(remoteCall.execute()).thenReturn(httpResponse);

    ContractResultResponse response = this.hederaService.getContractResult("51423");
    Assert.notNull(response);
    Assert.isTrue(response.equals(contractResultResponse));
  }

  @Test
  public void getContractResultButNotFoundIsExpected() throws IOException {
    ContractResultResponse contractResultResponse = new ContractResultResponse();
    contractResultResponse.setContractId("0.0.1");
    final String url = "https://url.com";

    final Response httpResponse =
        new Response.Builder()
            .request(new Request.Builder().url(url).build())
            .protocol(Protocol.HTTP_1_1)
            .code(404)
            .message("")
            .body(
                ResponseBody.create(
                    this.objectMapper.writeValueAsString(contractResultResponse),
                    MediaType.parse("application/json")))
            .build();

    when(remoteCall.execute()).thenReturn(httpResponse);

    NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () -> {
              this.hederaService.getContractResult("51423");
            });

    Assert.notNull(exception);
    Assert.isTrue(exception.getMessage().contains("Empty response -"));
  }

  @Test
  public void getContractResultButBadRequestIsExpected() throws IOException {
    ContractResultResponse contractResultResponse = new ContractResultResponse();
    contractResultResponse.setContractId("0.0.1");
    final String url = "https://url.com";

    final Response httpResponse =
        new Response.Builder()
            .request(new Request.Builder().url(url).build())
            .protocol(Protocol.HTTP_1_1)
            .code(400)
            .message("")
            .body(
                ResponseBody.create(
                    this.objectMapper.writeValueAsString(contractResultResponse),
                    MediaType.parse("application/json")))
            .build();

    when(remoteCall.execute()).thenReturn(httpResponse);

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              this.hederaService.getContractResult("51423");
            });

    Assert.notNull(exception);
    Assert.isTrue(exception.getMessage().contains("Invalid request -"));
  }

  @Test
  public void getBlock() throws IOException, NotFoundException {
    BlocksResponse blocksResponse = new BlocksResponse();
    BlockResponse blockResponse = new BlockResponse();
    blockResponse.setNumber(BigInteger.ONE);
    blocksResponse.setBlocks(Collections.singletonList(blockResponse));

    final Response httpResponse =
        new Response.Builder()
            .request(new Request.Builder().url("https://url.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("")
            .body(
                ResponseBody.create(
                    this.objectMapper.writeValueAsString(blocksResponse),
                    MediaType.parse("application/json")))
            .build();

    when(remoteCall.execute()).thenReturn(httpResponse);

    BlockResponse response = this.hederaService.getBlock(BigInteger.ONE);
    Assert.notNull(response);
    Assert.isTrue(response.equals(blockResponse));
  }

  @Test
  public void getEventForFilter() {
    ContractEventFilter eventFilter = new ContractEventFilter();
    ContractEventSpecification eventSpecification = new ContractEventSpecification();
    eventSpecification.setEventName("test");
    ParameterDefinition indexedParameter =
        new ParameterDefinition(0, ParameterType.build("STRING"));
    eventSpecification.setIndexedParameterDefinitions(Collections.singletonList(indexedParameter));
    ParameterDefinition nonIndexedParameter =
        new ParameterDefinition(0, ParameterType.build("STRING"));
    eventSpecification.setNonIndexedParameterDefinitions(
        Collections.singletonList(nonIndexedParameter));
    eventFilter.setEventSpecification(eventSpecification);

    HederaLogResponse mockLogResponse = new HederaLogResponse();
    mockLogResponse.setData("0x");

    ContractResultResponse mockContractResult = new ContractResultResponse();
    mockContractResult.setHash("0x");
    mockContractResult.setFrom("0x");
    mockContractResult.setBlockHash("0x");
    mockContractResult.setBlockNumber("0");
    mockContractResult.setTimestamp("1664196703.0000000");

    ContractEventDetails eventDetails = new ContractEventDetails();

    when(contractEventDetailsFactory.createEventDetails(
            eq(eventFilter),
            any(),
            eq(new BigInteger("1664196703")),
            eq(mockContractResult.getFrom())))
        .thenReturn(eventDetails);

    ContractEventDetails event =
        hederaService.getEventForFilter(eventFilter, mockLogResponse, mockContractResult);

    Assert.notNull(event);
    Assert.isTrue(event.equals(eventDetails));
  }
}
