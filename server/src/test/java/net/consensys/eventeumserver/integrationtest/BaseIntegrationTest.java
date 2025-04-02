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

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ExecutionException;
import net.consensys.eventeum.chain.util.Web3jUtil;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.dto.event.filter.ParameterDefinition;
import net.consensys.eventeum.dto.event.filter.ParameterType;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.endpoint.response.AddEventFilterResponse;
import net.consensys.eventeum.endpoint.response.MonitorTransactionsResponse;
import net.consensys.eventeum.integration.eventstore.db.repository.ContractEventDetailsRepository;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import net.consensys.eventeum.repository.ContractEventFilterRepository;
import net.consensys.eventeum.repository.TransactionMonitoringSpecRepository;
import net.consensys.eventeum.utils.JSON;
import net.consensys.eventeumserver.integrationtest.utils.SpringRestarter;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestContextManager;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

enum TopicTypesEnum {
  transaction,
  event,
  block,
  message
}

public class BaseIntegrationTest {

  protected static final BigInteger GAS_PRICE = BigInteger.valueOf(22_000_000_000L);
  protected static final BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000);
  protected static final String DUMMY_EVENT_NAME = "DummyEvent";
  protected static final String DUMMY_EVENT_NOT_ORDERED_NAME = "DummyEventNotOrdered";
  protected static final String FAKE_CONTRACT_ADDRESS =
      "0xb4f391500fc66e6a1ac5d345f58bdcbea66c1a6f";
  protected static final Credentials CREDS =
      Credentials.create("0x4d5db4107d237df6a3d58ee5f70ae63d73d7658d4026f2eefd2f204c81682cb7");
  protected static final String ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";
  private static final String PARITY_VOLUME_PATH = "target/parity";
  private static final Integer MONGODB_PORT = 27017;
  // "BytesValue" in hex
  private static final String BYTES_VALUE_HEX =
      "0x427974657356616c756500000000000000000000000000000000000000000000";
  public static boolean shouldPersistNodeVolume = false;
  private static FixedHostPortGenericContainer parityContainer;
  private static FixedHostPortGenericContainer mongoDBContainer;
  private final List<ContractEventDetails> broadcastContractEvents = new ArrayList<>();
  private final List<BlockDetails> broadcastBlockMessages = new ArrayList<>();
  private final List<TransactionDetails> broadcastTransactionMessages = new ArrayList<>();
  private final Map<String, ContractEventFilter> registeredFilters = new HashMap<>();
  private final List<String> registeredTransactionMonitorIds = new ArrayList<>();
  @LocalServerPort private int port;

  @Autowired(required = false)
  private ContractEventFilterRepository filterRepo;

  @Autowired(required = false)
  private TransactionMonitoringSpecRepository txFilterRepo;

  @Autowired(required = false)
  private ContractEventDetailsRepository eventDetailsRepository;

  private RestTemplate restTemplate;
  private String restUrl;
  private Web3j web3j;
  private Admin admin;
  private String dummyEventFilterId;
  private String dummyEventNotOrderedFilterId;

  @BeforeAll
  public static void setupEnvironment() throws Exception {
    StubEventStoreService.start();

    final File file = new File(PARITY_VOLUME_PATH);
    file.mkdirs();

    startParity();
    startMongo();
  }

  @AfterAll
  public static void teardownEnvironment() throws Exception {
    StubEventStoreService.stop();

    shouldPersistNodeVolume = true;
    stopParity();

    stopMongo();

    try {
      // Clear parity data
      final File file = new File(PARITY_VOLUME_PATH);
      FileUtils.deleteDirectory(file);
    } catch (Throwable t) {
      System.err.println("Cannot delete parity directory: " + t.getMessage());
      t.printStackTrace();
      // When running on circleci the parity dir cannot be deleted but this does no affect tests
    }
  }

  protected static byte[] stringToBytes(String string) {
    byte[] byteValue = string.getBytes();
    byte[] byteValueLen32 = new byte[32];
    System.arraycopy(byteValue, 0, byteValueLen32, 0, byteValue.length);
    return byteValueLen32;
  }

  protected static void startParity() {
    parityContainer = new FixedHostPortGenericContainer("kauriorg/parity-docker:latest");
    parityContainer.waitingFor(Wait.forListeningPort());
    parityContainer.withFixedExposedPort(8545, 8545);
    parityContainer.withFixedExposedPort(8546, 8546);
    if (shouldPersistNodeVolume) {
      parityContainer.withFileSystemBind(
          PARITY_VOLUME_PATH, "/root/.local/share/io.parity.ethereum/", BindMode.READ_WRITE);
    }
    parityContainer.addEnv("NO_BLOCKS", "true");
    parityContainer.start();

    waitForParityToStart(10000, Web3j.build(new HttpService("http://localhost:8545")));
  }

  protected static void startMongo() {
    if (isLocalPortFree(MONGODB_PORT)) {
      mongoDBContainer = new FixedHostPortGenericContainer("mongo:4.0.10");
      mongoDBContainer.waitingFor(Wait.forListeningPort());
      mongoDBContainer.withFixedExposedPort(MONGODB_PORT, MONGODB_PORT);
      mongoDBContainer.waitingFor(
          Wait.forLogMessage(".*waiting for connections on port 27017.*", 1));

      mongoDBContainer.start();
      System.setProperty(
          "spring.data.mongodb.uri",
          "mongodb://"
              + mongoDBContainer.getHost()
              + ":"
              + mongoDBContainer.getMappedPort(MONGODB_PORT).toString()
              + "/mongodb");
    }
  }

  protected static void stopParity() {
    parityContainer.stop();
  }

  protected static void stopMongo() {
    if (mongoDBContainer != null) {
      mongoDBContainer.stop();
    }
  }

  public static Boolean isLocalPortFree(Integer port) {
    try {
      new ServerSocket(port).close();
      return true;
    } catch (IOException ignored) {
      return false;
    }
  }

  private static void waitForParityToStart(long timeToWait, Web3j web3j) {
    final long startTime = System.currentTimeMillis();

    while (true) {
      if (System.currentTimeMillis() > startTime + timeToWait) {
        throw new IllegalStateException("Parity failed to start...");
      }

      try {
        web3j.web3ClientVersion().send();
        break;
      } catch (Throwable t) {
        // If an error occurs, the node is not yet up
      }

      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @BeforeEach
  public void setUp() throws Exception {

    initRestTemplate();
    this.web3j = Web3j.build(new HttpService("http://localhost:8545"));
    this.admin = Admin.build(new HttpService("http://localhost:8545"));

    this.web3j
        .ethSendTransaction(
            Transaction.createEtherTransaction(
                this.web3j.ethAccounts().send().getAccounts().getFirst(),
                this.web3j
                    .ethGetTransactionCount(
                        this.web3j.ethAccounts().send().getAccounts().getFirst(),
                        DefaultBlockParameterName.fromString("latest"))
                    .send()
                    .getTransactionCount(),
                BigInteger.valueOf(2000),
                BigInteger.valueOf(6721975),
                CREDS.getAddress(),
                new BigInteger("9460000000000000000")))
        .send();

    dummyEventFilterId = UUID.randomUUID().toString();
    dummyEventNotOrderedFilterId = UUID.randomUUID().toString();

    clearMessages();
  }

  @AfterEach
  public void cleanup() {
    final ArrayList<String> filterIds = new ArrayList<>(registeredFilters.keySet());

    try {
      filterIds.forEach(filterId -> unregisterEventFilter(filterId));
    } catch (Throwable t) {
      t.printStackTrace();
    }

    filterRepo.deleteAll();

    if (eventDetailsRepository != null) {
      eventDetailsRepository.deleteAll();
    }

    // Get around concurrent modification exception
    try {
      new ArrayList<>(registeredTransactionMonitorIds).forEach(this::unregisterTransactionMonitor);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  protected List<ContractEventDetails> getBroadcastContractEvents() {
    return broadcastContractEvents;
  }

  protected List<BlockDetails> getBroadcastBlockMessages() {
    return broadcastBlockMessages;
  }

  protected List<TransactionDetails> getBroadcastTransactionMessages() {
    return broadcastTransactionMessages;
  }

  protected ContractEventFilterRepository getFilterRepo() {
    return filterRepo;
  }

  protected TransactionMonitoringSpecRepository getTxFilterRepo() {
    return txFilterRepo;
  }

  protected EventEmitter deployEventEmitterContract() throws Exception {
    return EventEmitter.deploy(web3j, CREDS, GAS_PRICE, GAS_LIMIT).send();
  }

  protected ContractEventFilter registerDummyEventFilter(String contractAddress) {
    return registerEventFilter(createDummyEventFilter(contractAddress));
  }

  protected ContractEventFilter registerEventFilter(ContractEventFilter filter) {
    final ResponseEntity<AddEventFilterResponse> response =
        restTemplate.postForEntity(
            restUrl + "/api/rest/v1/event-filter", filter, AddEventFilterResponse.class);

    filter.setId(response.getBody().getId());

    registeredFilters.put(filter.getId(), filter);
    return filter;
  }

  protected List<ContractEventFilter> listEventFilters() {
    final ResponseEntity<List<ContractEventFilter>> response =
        restTemplate.exchange(
            restUrl + "/api/rest/v1/event-filter",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ContractEventFilter>>() {});

    List<ContractEventFilter> contractEventFilters = response.getBody();

    return contractEventFilters;
  }

  protected String monitorTransaction(TransactionMonitoringSpec monitorSpec) {
    final ResponseEntity<MonitorTransactionsResponse> response =
        restTemplate.postForEntity(
            restUrl + "/api/rest/v1/transaction", monitorSpec, MonitorTransactionsResponse.class);

    registeredTransactionMonitorIds.add(response.getBody().getId());
    return response.getBody().getId();
  }

  protected void unregisterTransactionMonitor(String monitorId) {
    restTemplate.delete(restUrl + "/api/rest/v1/transaction/" + monitorId);

    registeredTransactionMonitorIds.remove(monitorId);
  }

  protected void unregisterDummyEventFilter() {
    unregisterEventFilter(getDummyEventFilterId());
  }

  protected void unregisterEventFilter(String filterId) {
    restTemplate.delete(restUrl + "/api/rest/v1/event-filter/" + filterId);

    registeredFilters.remove(filterId);
  }

  protected boolean unlockAccount() throws IOException {
    PersonalUnlockAccount unlock = admin.personalUnlockAccount(CREDS.getAddress(), "").send();

    try {
      return unlock.accountUnlocked();
    } catch (NullPointerException npe) {
      // NPE thrown in parity if account is unlocked at startup
      return true;
    }
  }

  protected void triggerBlocks(Integer numberOfBlocks)
      throws ExecutionException, InterruptedException, IOException {
    if (!unlockAccount()) {
      throw new RuntimeException("Unable to unlock account");
    }

    for (int i = 0; i < numberOfBlocks; i++) {
      sendTransaction();
    }
  }

  protected String sendTransaction() throws ExecutionException, InterruptedException, IOException {
    EthGetTransactionCount ethGetTransactionCount =
        web3j
            .ethGetTransactionCount(CREDS.getAddress(), DefaultBlockParameterName.LATEST)
            .sendAsync()
            .get();
    BigInteger nonce = ethGetTransactionCount.getTransactionCount();

    final Transaction tx =
        Transaction.createEtherTransaction(
            CREDS.getAddress(), nonce, GAS_PRICE, GAS_LIMIT, ZERO_ADDRESS, BigInteger.ONE);

    EthSendTransaction response = web3j.ethSendTransaction(tx).send();

    return response.getTransactionHash();
  }

  protected String createRawSignedTransactionHex() throws ExecutionException, InterruptedException {
    return createRawSignedTransactionHex(ZERO_ADDRESS);
  }

  protected String createRawSignedTransactionHex(BigInteger nonce)
      throws ExecutionException, InterruptedException {
    return createRawSignedTransactionHex(ZERO_ADDRESS, nonce);
  }

  protected BigInteger getNonce() throws ExecutionException, InterruptedException {
    final EthGetTransactionCount ethGetTransactionCount =
        web3j
            .ethGetTransactionCount(CREDS.getAddress(), DefaultBlockParameterName.LATEST)
            .sendAsync()
            .get();

    final BigInteger nonce = ethGetTransactionCount.getTransactionCount();

    return nonce;
  }

  protected String createRawSignedTransactionHex(String toAddress)
      throws ExecutionException, InterruptedException {

    return createRawSignedTransactionHex(toAddress, getNonce());
  }

  protected String createRawSignedTransactionHex(String toAddress, BigInteger nonce)
      throws ExecutionException, InterruptedException {

    final RawTransaction rawTransaction =
        RawTransaction.createEtherTransaction(
            nonce, GAS_PRICE, GAS_LIMIT, toAddress, BigInteger.ONE);

    final byte[] signedTx = TransactionEncoder.signMessage(rawTransaction, CREDS);

    return Numeric.toHexString(signedTx);
  }

  protected String sendRawTransaction(String signedTxHex)
      throws ExecutionException, InterruptedException {

    final EthSendTransaction ethSendTransaction =
        web3j.ethSendRawTransaction(signedTxHex).sendAsync().get();
    return ethSendTransaction.getTransactionHash();
  }

  protected void verifyDummyEventDetails(
      ContractEventFilter registeredFilter,
      ContractEventDetails eventDetails,
      ContractEventStatus status) {

    verifyDummyEventDetails(
        registeredFilter,
        eventDetails,
        status,
        BYTES_VALUE_HEX,
        Keys.toChecksumAddress(CREDS.getAddress()),
        BigInteger.TEN,
        "StringValue");
  }

  protected void verifyDummyEventDetails(
      ContractEventFilter registeredFilter,
      ContractEventDetails eventDetails,
      ContractEventStatus status,
      String valueOne,
      String valueTwo,
      BigInteger valueThree,
      String valueFour) {
    assertEquals(registeredFilter.getEventSpecification().getEventName(), eventDetails.getName());
    assertEquals(status, eventDetails.getStatus());
    assertEquals(valueOne, eventDetails.getIndexedParameters().getFirst().getValue());
    assertEquals(valueTwo, eventDetails.getIndexedParameters().get(1).getValue());
    assertEquals(valueThree, eventDetails.getNonIndexedParameters().getFirst().getValue());
    assertEquals(valueFour, eventDetails.getNonIndexedParameters().get(1).getValue());
    assertEquals(BigInteger.ONE, eventDetails.getNonIndexedParameters().get(2).getValue());
    assertEquals(
        Web3jUtil.getSignature(registeredFilter.getEventSpecification()),
        eventDetails.getEventSpecificationSignature());
    assertNotNull(eventDetails.getTimestamp());
  }

  protected void waitForBroadcast() throws InterruptedException {
    Thread.sleep(3000);
  }

  protected void waitForFilterPoll() throws InterruptedException {
    Thread.sleep(1000);
  }

  protected void clearMessages() {
    getBroadcastContractEvents().clear();
    getBroadcastBlockMessages().clear();
    getBroadcastTransactionMessages().clear();
  }

  protected void waitForContractEventMessages(int expectedContractEventMessages) {
    waitForMessages(
        expectedContractEventMessages, getBroadcastContractEvents(), true, TopicTypesEnum.event);
  }

  protected void waitForBlockMessages(int expectedBlockMessages) {
    waitForMessages(expectedBlockMessages, getBroadcastBlockMessages(), true, TopicTypesEnum.block);
  }

  protected void waitForTransactionMessages(int expectedTransactionMessages) {
    waitForMessages(
        expectedTransactionMessages,
        getBroadcastTransactionMessages(),
        true,
        TopicTypesEnum.transaction);
  }

  protected void waitForTransactionMessages(
      int expectedTransactionMessages, boolean failOnTimeout) {
    waitForMessages(
        expectedTransactionMessages,
        getBroadcastTransactionMessages(),
        failOnTimeout,
        TopicTypesEnum.transaction);
  }

  protected <T> boolean waitForMessages(int expectedMessageCount, List<T> messages) {
    return waitForMessages(expectedMessageCount, messages, true, TopicTypesEnum.message);
  }

  protected <T> boolean waitForMessages(
      int expectedMessageCount, List<T> messages, boolean failOnTimeout, TopicTypesEnum type) {
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    final long startTime = System.currentTimeMillis();
    while (true) {
      if (messages.size() >= expectedMessageCount) {
        return true;
      }

      if (System.currentTimeMillis() > startTime + 20000) {
        if (failOnTimeout) {
          fail(generateFailureMessage(expectedMessageCount, messages, type));
        }

        return false;
      }

      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  protected String getDummyEventFilterId() {
    return dummyEventFilterId;
  }

  protected ContractEventFilter createDummyEventFilter(String contractAddress) {

    final ContractEventSpecification eventSpec = new ContractEventSpecification();
    eventSpec.setIndexedParameterDefinitions(
        Arrays.asList(
            new ParameterDefinition(0, ParameterType.build("BYTES32")),
            new ParameterDefinition(1, ParameterType.build("ADDRESS"))));

    eventSpec.setNonIndexedParameterDefinitions(
        Arrays.asList(
            new ParameterDefinition(2, ParameterType.build("UINT256")),
            new ParameterDefinition(3, ParameterType.build("STRING")),
            new ParameterDefinition(4, ParameterType.build("UINT8"))));

    eventSpec.setEventName(DUMMY_EVENT_NAME);

    return createFilter(getDummyEventFilterId(), contractAddress, eventSpec);
  }

  protected String getDummyEventNotOrderedFilterId() {
    return dummyEventNotOrderedFilterId;
  }

  protected ContractEventFilter createDummyEventNotOrderedFilter(String contractAddress) {

    final ContractEventSpecification eventSpec = new ContractEventSpecification();
    eventSpec.setIndexedParameterDefinitions(
        Arrays.asList(
            new ParameterDefinition(0, ParameterType.build("BYTES32")),
            new ParameterDefinition(2, ParameterType.build("ADDRESS"))));

    eventSpec.setNonIndexedParameterDefinitions(
        Arrays.asList(
            new ParameterDefinition(1, ParameterType.build("UINT256")),
            new ParameterDefinition(3, ParameterType.build("STRING")),
            new ParameterDefinition(4, ParameterType.build("UINT8"))));

    eventSpec.setEventName(DUMMY_EVENT_NOT_ORDERED_NAME);

    return createFilter(getDummyEventNotOrderedFilterId(), contractAddress, eventSpec);
  }

  protected void restartEventeum(Runnable stoppedLogic, TestContextManager testContextManager) {

    SpringRestarter sr = SpringRestarter.getInstance();
    sr.init(testContextManager);
    sr.restart(stoppedLogic);

    restUrl = "http://localhost:" + port;
    restTemplate = new RestTemplate();
  }

  protected ContractEventFilter doRegisterAndUnregister(String contractAddress)
      throws InterruptedException {
    final ContractEventFilter registeredFilter = registerDummyEventFilter(contractAddress);
    Optional<ContractEventFilter> saved = getFilterRepo().findById(getDummyEventFilterId());
    assertEquals(registeredFilter, saved.get());

    Thread.sleep(1000);
    unregisterDummyEventFilter();
    Thread.sleep(1000);

    saved = getFilterRepo().findById(getDummyEventFilterId());
    assertFalse(saved.isPresent());

    Thread.sleep(2000);

    return registeredFilter;
  }

  protected ContractEventFilter createFilter(
      String id, String contractAddress, ContractEventSpecification eventSpec) {
    final ContractEventFilter contractEventFilter = new ContractEventFilter();
    contractEventFilter.setId(id);
    contractEventFilter.setContractAddress(contractAddress);
    contractEventFilter.setEventSpecification(eventSpec);

    return contractEventFilter;
  }

  private <T> String generateFailureMessage(
      int expectedMessageCount, List<T> messages, TopicTypesEnum type) {
    final StringBuilder builder = new StringBuilder("Failed to receive all expected " + type);
    builder.append("\n");
    builder.append("Expected " + type + " count: " + expectedMessageCount);
    builder.append(", received: " + messages.size());
    builder.append("\n\n");
    builder.append(
        type.toString().substring(0, 1).toUpperCase()
            + type.toString().substring(1)
            + " received: "
            + JSON.stringify(messages));
    builder.append("\n\n");
    builder.append("Registered event filters:");
    builder.append("\n\n");
    builder.append(JSON.stringify(IterableUtils.toList(getFilterRepo().findAll())));
    builder.append("\n\n");
    builder.append("Registered transaction filters:");
    builder.append("\n\n");
    builder.append(JSON.stringify(IterableUtils.toList(getTxFilterRepo().findAll())));
    builder.append("\n\n");
    builder.append("ContractEventDetails entries:");
    builder.append("\n\n");
    try {
      builder.append(JSON.stringify(IterableUtils.toList(eventDetailsRepository.findAll())));
    } catch (Exception ex) {
    }

    return builder.toString();
  }

  private void initRestTemplate() {
    restUrl = "http://localhost:" + port;
    restTemplate = new RestTemplate();
  }
}
