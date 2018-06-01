package net.consensys.eventeum.integrationtest;

import junit.framework.TestCase;
import net.consensys.eventeum.chain.util.Web3jUtil;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.dto.event.filter.ParameterDefinition;
import net.consensys.eventeum.dto.event.filter.ParameterType;
import net.consensys.eventeum.dto.message.Message;
import net.consensys.eventeum.endpoint.response.AddEventFilterResponse;
import net.consensys.eventeum.repository.ContractEventFilterRepository;
import net.consensys.eventeum.utils.JSON;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.web.client.RestTemplate;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class BaseIntegrationTest {

    protected static final BigInteger GAS_PRICE = BigInteger.valueOf(22_000_000_000L);
    protected static final BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000);

    protected static final String DUMMY_EVENT_NAME = "DummyEvent";
    protected static final String DUMMY_EVENT_NOT_ORDERED_NAME = "DummyEventNotOrdered";
    protected static final String FAKE_CONTRACT_ADDRESS = "0xb4f391500fc66e6a1ac5d345f58bdcbea66c1a6f";

    protected static final Credentials CREDS = Credentials.create("0x4d5db4107d237df6a3d58ee5f70ae63d73d7658d4026f2eefd2f204c81682cb7");
    //protected static final Credentials CREDS = Credentials.create("4f3edf983ac636a65a842ce7c78d9aa706d3b113bce9c46f30d7d21715b23b1d");

    private List<Message<ContractEventDetails>> broadcastContractEventMessages = new ArrayList<>();
    private List<Message<ContractEventFilter>> broadcastFilterEventMessages = new ArrayList<>();

    @LocalServerPort
    private int port = 12345;

    @Autowired
    private ContractEventFilterRepository filterRepo;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    private RestTemplate restTemplate;

    private String restUrl;

    private Web3j web3j;

    private Admin admin;

    private String dummyEventFilterId;

    private String dummyEventNotOrderedFilterId;

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true);

    @BeforeClass
    public static void setupEnvironment() throws IOException {
        StubEventStoreService.start();
    }

    @Before
    public void setUp() throws Exception {
        restUrl = "http://localhost:" + port;
        restTemplate = new RestTemplate();
        this.web3j = Web3j.build(new HttpService("http://localhost:8545"));
        this.admin = Admin.build(new HttpService("http://localhost:8545"));

        dummyEventFilterId = UUID.randomUUID().toString();
        dummyEventNotOrderedFilterId = UUID.randomUUID().toString();

        Thread.sleep(15000);
        clearMessages();

//        // wait until the partitions are assigned
//        for (MessageListenerContainer messageListenerContainer : kafkaListenerEndpointRegistry
//                .getListenerContainers()) {
//            ContainerTestUtils.waitForAssignment(messageListenerContainer, 3);
//        }
    }

    @AfterClass
    public static void teardownEnvironment() {
        StubEventStoreService.stop();
    }

    @After
    public void cleanup() {
        filterRepo.delete(getDummyEventFilterId());
    }

    @KafkaListener(topics = "#{kafkaSettings.filterEventsTopic}", groupId="testGroup")
    public void onFilterEventMessage(Message<ContractEventFilter> message) {
        broadcastFilterEventMessages.add(message);
    }


    @KafkaListener(topics = "#{kafkaSettings.contractEventsTopic}", groupId="testGroup")
    public void onContractEventMessage(Message<ContractEventDetails> contractEventMessage) {
        System.out.println("Message received: " + JSON.stringify(contractEventMessage));
        broadcastContractEventMessages.add(contractEventMessage);
    }

    protected List<Message<ContractEventDetails>> getBroadcastContractEventMessages() {
        return broadcastContractEventMessages;
    }

    protected List<Message<ContractEventFilter>> getBroadcastFilterEventMessages() {
        return broadcastFilterEventMessages;
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
        return filter;
    }

    protected void unregisterDummyEventFilter() {
        unregisterEventFilter(getDummyEventFilterId());
    }

    protected void unregisterEventFilter(String filterId) {
        restTemplate.delete(restUrl + "/api/rest/v1/event-filter/" + filterId);
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
        assertEquals(registeredFilter.getEventSpecification().getEventName(), eventDetails.getName());
        assertEquals(status, eventDetails.getStatus());
        assertEquals("BytesValue", eventDetails.getIndexedParameters().get(0).getValue());
        assertEquals(CREDS.getAddress(), eventDetails.getIndexedParameters().get(1).getValue());
        assertEquals(BigInteger.TEN, eventDetails.getNonIndexedParameters().get(0).getValue());
        assertEquals("StringValue", eventDetails.getNonIndexedParameters().get(1).getValue());
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
        Thread.sleep(2000);
    }

    protected void waitForFilterPoll() throws InterruptedException {
        Thread.sleep(15000);
    }

    protected void clearMessages() {
        broadcastContractEventMessages.clear();
        broadcastFilterEventMessages.clear();
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
            if (broadcastContractEventMessages.size() == expectedContractEventMessages) {
                break;
            }

            if (System.currentTimeMillis() > startTime + 20000) {
                final StringBuilder builder = new StringBuilder("Failed to receive all expected messages");
                builder.append("\n");
                builder.append("Expected contract event messages: " + expectedContractEventMessages);
                builder.append(", received: " + broadcastContractEventMessages.size());

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
                              new ParameterDefinition(3, ParameterType.STRING)));

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
                              new ParameterDefinition(3, ParameterType.STRING)));

        eventSpec.setEventName(DUMMY_EVENT_NOT_ORDERED_NAME);

        return createFilter(getDummyEventNotOrderedFilterId(), contractAddress, eventSpec);
    }

    private ContractEventFilter createFilter(String id, String contractAddress, ContractEventSpecification eventSpec) {
        final ContractEventFilter contractEventFilter = new ContractEventFilter();
        contractEventFilter.setId(id);
        contractEventFilter.setContractAddress(contractAddress);
        contractEventFilter.setEventSpecification(eventSpec);

        return contractEventFilter;
    }
}
