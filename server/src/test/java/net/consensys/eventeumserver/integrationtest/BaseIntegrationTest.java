package net.consensys.eventeumserver.integrationtest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import net.consensys.eventeum.chain.service.health.NodeHealthCheckService;
import net.consensys.eventeum.chain.util.Web3jUtil;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.dto.event.filter.ParameterDefinition;
import net.consensys.eventeum.dto.event.filter.ParameterType;
import net.consensys.eventeum.endpoint.response.AddEventFilterResponse;
import net.consensys.eventeum.repository.ContractEventFilterRepository;
import net.consensys.eventeum.utils.JSON;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.http.HttpService;
import scala.math.BigInt;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class BaseIntegrationTest {

    private static final String PARITY_VOLUME_PATH = "target/parity";

    protected static final BigInteger GAS_PRICE = BigInteger.valueOf(22_000_000_000L);
    protected static final BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000);

    protected static final String DUMMY_EVENT_NAME = "DummyEvent";
    protected static final String DUMMY_EVENT_NOT_ORDERED_NAME = "DummyEventNotOrdered";
    protected static final String FAKE_CONTRACT_ADDRESS = "0xb4f391500fc66e6a1ac5d345f58bdcbea66c1a6f";

    protected static final Credentials CREDS = Credentials.create("0x4d5db4107d237df6a3d58ee5f70ae63d73d7658d4026f2eefd2f204c81682cb7");

    private static FixedHostPortGenericContainer parityContainer;

    private List<ContractEventDetails> broadcastContractEvents = new ArrayList<>();

    private List<BlockDetails> broadcastBlockMessages = new ArrayList<>();

    @LocalServerPort
    private int port = 12345;

    @Autowired
    private ContractEventFilterRepository filterRepo;

    private RestTemplate restTemplate;

    private String restUrl;

    private Web3j web3j;

    private Admin admin;

    private String dummyEventFilterId;

    private String dummyEventNotOrderedFilterId;

    private Map<String, ContractEventFilter> registeredFilters = new HashMap<>();

    @BeforeClass
    public static void setupEnvironment() throws IOException {
        StubEventStoreService.start();

        final File file = new File(PARITY_VOLUME_PATH);
        file.mkdirs();

        startParity();
    }

    @Before
    public void setUp() throws Exception {

        restUrl = "http://localhost:" + port;
        restTemplate = new RestTemplate();
        this.web3j = Web3j.build(new HttpService("http://localhost:8545"));
        this.admin = Admin.build(new HttpService("http://localhost:8545"));

        this.web3j.ethSendTransaction(Transaction.createEtherTransaction(
                this.web3j.ethAccounts().send().getAccounts().get(0),

                this.web3j.ethGetTransactionCount(
                    this.web3j.ethAccounts().send().getAccounts().get(0),
                        DefaultBlockParameterName.fromString("latest")
                ).send().getTransactionCount(),

                BigInteger.valueOf(2000),
                BigInteger.valueOf(6721975),
                CREDS.getAddress(),
                new BigInteger("9460000000000000000"))
        ).send();

        dummyEventFilterId = UUID.randomUUID().toString();
        dummyEventNotOrderedFilterId = UUID.randomUUID().toString();

        clearMessages();

    }

    @AfterClass
    public static void teardownEnvironment() throws Exception {
        StubEventStoreService.stop();

        stopParity();

        try {
            //Clear parity data
            final File file = new File(PARITY_VOLUME_PATH);
            FileUtils.deleteDirectory(file);
        } catch (Throwable t) {
            //When running on circleci the parity dir cannot be deleted but this does no affect tests
        }
    }

    @After
    public void cleanup() {
        final ArrayList<String> filterIds = new ArrayList<>(registeredFilters.keySet());
        filterIds.forEach(filterId -> unregisterEventFilter(filterId));

        filterRepo.deleteAll();
    }

    protected List<ContractEventDetails> getBroadcastContractEvents() {
        return broadcastContractEvents;
    }

    protected List<BlockDetails> getBroadcastBlockMessages() {
        return broadcastBlockMessages;
    }

    protected ContractEventFilterRepository getFilterRepo() {
        return filterRepo;
    }

    protected EventEmitter deployEventEmitterContract() throws Exception {
        return EventEmitter.deploy(web3j, CREDS, GAS_PRICE, GAS_LIMIT).send();
    }

    protected ContractEventFilter registerDummyEventFilter(String contractAddress) {
        return registerEventFilter(createDummyEventFilter(contractAddress));
    }

    protected ContractEventFilter registerEventFilter(ContractEventFilter filter) {
        final ResponseEntity<AddEventFilterResponse> response =
                restTemplate.postForEntity(restUrl + "/api/rest/v1/event-filter", filter, AddEventFilterResponse.class);

        filter.setId(response.getBody().getId());

        registeredFilters.put(filter.getId(), filter);
        return filter;
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
            //NPE thrown in parity if account is unlocked at startup
            return true;
        }
    }

    protected void triggerBlocks(Integer numberOfBlocks) throws ExecutionException, InterruptedException, IOException {
        if (!unlockAccount()) {
            throw new RuntimeException("Unable to unlock account");
        }

        for (int i = 0; i < numberOfBlocks; i++) {
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                    CREDS.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();

            final Transaction tx = Transaction.createEtherTransaction(CREDS.getAddress(),
                    nonce, GAS_PRICE, GAS_LIMIT, "0x0000000000000000000000000000000000000000", BigInteger.ONE);
            web3j.ethSendTransaction(tx).send();
        }
    }

    protected void verifyDummyEventDetails(ContractEventFilter registeredFilter,
                                         ContractEventDetails eventDetails, ContractEventStatus status) {
        verifyDummyEventDetails(registeredFilter, eventDetails, status,
                "BytesValue", Keys.toChecksumAddress(CREDS.getAddress()), BigInteger.TEN, "StringValue");
    }

    protected void verifyDummyEventDetails(ContractEventFilter registeredFilter,
                                           ContractEventDetails eventDetails,
                                           ContractEventStatus status,
                                           String valueOne,
                                           String valueTwo,
                                           BigInteger valueThree,
                                           String valueFour) {
        assertEquals(registeredFilter.getEventSpecification().getEventName(), eventDetails.getName());
        assertEquals(status, eventDetails.getStatus());
        assertEquals(valueOne, eventDetails.getIndexedParameters().get(0).getValue());
        assertEquals(valueTwo,
                eventDetails.getIndexedParameters().get(1).getValue());
        assertEquals(valueThree, eventDetails.getNonIndexedParameters().get(0).getValue());
        assertEquals(valueFour, eventDetails.getNonIndexedParameters().get(1).getValue());
        assertEquals(BigInteger.ONE, eventDetails.getNonIndexedParameters().get(2).getValue());
        assertEquals(Web3jUtil.getSignature(registeredFilter.getEventSpecification()),
                eventDetails.getEventSpecificationSignature());
    }

    protected byte[] stringToBytes(String string) {
        byte[] byteValue = string.getBytes();
        byte[] byteValueLen32 = new byte[32];
        System.arraycopy(byteValue, 0, byteValueLen32, 0, byteValue.length);
        return byteValueLen32;
    }

    protected void waitForBroadcast() throws InterruptedException {
        Thread.sleep(3000);
    }

    protected void waitForFilterPoll() throws InterruptedException {
        Thread.sleep(15000);
    }

    protected void clearMessages() {
        getBroadcastContractEvents().clear();
        getBroadcastBlockMessages().clear();
    }

    protected void waitForContractEventMessages(int expectedContractEventMessages) {
        //Wait for an initial 2 seconds (this is usually enough time and is needed
        //in order to catch failures when no messages are expected on error topic but one arrives)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Wait for another 20 seconds maximum if messages have not yet arrived
        final long startTime = System.currentTimeMillis();
        while(true) {
            if (getBroadcastContractEvents().size() == expectedContractEventMessages) {
                break;
            }

            if (System.currentTimeMillis() > startTime + 20000) {
                final StringBuilder builder = new StringBuilder("Failed to receive all expected messages");
                builder.append("\n");
                builder.append("Expected contract event messages: " + expectedContractEventMessages);
                builder.append(", received: " + getBroadcastContractEvents().size());
                builder.append("\n");
                builder.append(JSON.stringify(getBroadcastContractEvents()));

                TestCase.fail(builder.toString());
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void waitForBlockMessages(int expectedBlockMessages) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final long startTime = System.currentTimeMillis();
        while(true) {
            if (getBroadcastBlockMessages().size() >= expectedBlockMessages) {
                break;
            }

            if (System.currentTimeMillis() > startTime + 20000) {
                final StringBuilder builder = new StringBuilder("Failed to receive all expected messages");
                builder.append("\n");
                builder.append("Expected block messages: " + expectedBlockMessages);
                builder.append(", received: " + broadcastBlockMessages.size());

                TestCase.fail(builder.toString());
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
                Arrays.asList(new ParameterDefinition(0, ParameterType.BYTES32),
                              new ParameterDefinition(1, ParameterType.ADDRESS)));

        eventSpec.setNonIndexedParameterDefinitions(
                Arrays.asList(new ParameterDefinition(2, ParameterType.UINT256),
                              new ParameterDefinition(3, ParameterType.STRING),
                              new ParameterDefinition(4, ParameterType.UINT8)));

        eventSpec.setEventName(DUMMY_EVENT_NAME);

        return createFilter(getDummyEventFilterId(), contractAddress, eventSpec);
    }

    protected String getDummyEventNotOrderedFilterId() {
        return dummyEventNotOrderedFilterId;
    }

    protected ContractEventFilter createDummyEventNotOrderedFilter(String contractAddress) {

        final ContractEventSpecification eventSpec = new ContractEventSpecification();
        eventSpec.setIndexedParameterDefinitions(
                Arrays.asList(new ParameterDefinition(0, ParameterType.BYTES32),
                              new ParameterDefinition(2, ParameterType.ADDRESS)));

        eventSpec.setNonIndexedParameterDefinitions(
                Arrays.asList(new ParameterDefinition(1, ParameterType.UINT256),
                              new ParameterDefinition(3, ParameterType.STRING),
                              new ParameterDefinition(4, ParameterType.UINT8)));

        eventSpec.setEventName(DUMMY_EVENT_NOT_ORDERED_NAME);

        return createFilter(getDummyEventNotOrderedFilterId(), contractAddress, eventSpec);
    }

    protected static void startParity() {
        parityContainer = new FixedHostPortGenericContainer("kauriorg/parity-docker:latest");
        parityContainer.waitingFor(Wait.forListeningPort());
        parityContainer.withFixedExposedPort(8545, 8545);
        parityContainer.withFixedExposedPort(8546, 8546);
        parityContainer.withFileSystemBind(PARITY_VOLUME_PATH,
                "/root/.local/share/io.parity.ethereum/", BindMode.READ_WRITE);
        parityContainer.addEnv("NO_BLOCKS", "true");
        parityContainer.start();

        waitForParityToStart(10000, Web3j.build(new HttpService("http://localhost:8545")));
    }

    protected static void stopParity() {
        parityContainer.stop();
    }

    private ContractEventFilter createFilter(String id, String contractAddress, ContractEventSpecification eventSpec) {
        final ContractEventFilter contractEventFilter = new ContractEventFilter();
        contractEventFilter.setId(id);
        contractEventFilter.setContractAddress(contractAddress);
        contractEventFilter.setEventSpecification(eventSpec);

        return contractEventFilter;
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
                //If an error occurs, the node is not yet up
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();;
            }
        }
    }
}
