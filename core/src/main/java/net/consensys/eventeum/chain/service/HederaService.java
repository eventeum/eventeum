package net.consensys.eventeum.chain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.contract.ContractEventListener;
import net.consensys.eventeum.chain.factory.ContractEventDetailsFactory;
import net.consensys.eventeum.chain.service.block.EventBlockManagementService;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.TransactionReceipt;
import net.consensys.eventeum.chain.service.domain.io.*;
import net.consensys.eventeum.chain.service.domain.wrapper.HederaBlock;
import net.consensys.eventeum.chain.service.domain.wrapper.HederaTransactionReceipt;
import net.consensys.eventeum.chain.service.domain.wrapper.Web3jTransaction;
import net.consensys.eventeum.chain.settings.Node;
import net.consensys.eventeum.chain.util.Web3jUtil;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.model.FilterSubscription;
import net.consensys.eventeum.service.AsyncTaskService;
import net.consensys.eventeum.service.EventStoreService;
import net.consensys.eventeum.service.SubscriptionService;
import net.consensys.eventeum.service.exception.NotFoundException;
import net.consensys.eventeum.utils.AtomicBigInteger;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.web3j.protocol.core.methods.response.Log;

@Slf4j
public class HederaService implements BlockchainService {

  public static final String CONTRACT_VAR = "{Contract}";
  public static final String TIMESTAMP = "timestamp";
  public static final String LTE = "lte";
  public static final String GTE = "gte";
  public static final String LIMIT = "limit";
  public static final String ORDER = "order";
  public static final String ASC = "asc";
  private static final String API_VERSION_PATH = "/api/v1";
  private static final String CONTRACT_RESULTS_PATH = "/contracts/results";
  private static final String BLOCKS_PATH = "/blocks";
  private static final String LOGS_PATH = "/contracts/{Contract}/results/logs";
  private static final String CLIENT_VERSION = "v1";
  private static final String EVENT_EXECUTOR_NAME = "EVENT";
  private static final String BLOCK_NUMBER = "block.number";
  private static final String NEXT = "next";

  private final ContractEventDetailsFactory eventDetailsFactory;

  private final EventStoreService eventStoreService;

  private final OkHttpClient okHttpClient;

  private final ObjectMapper objectMapper;

  private final String nodeName;

  private final String nodeUrl;

  private final Map<String, String> nodeHeaders;

  private final String nodeLimitPerRequest;

  private final BigInteger maxRetries;

  private final ModelMapper modelMapper;
  private final ScheduledExecutorService scheduledExecutorService;

  private final EventBlockManagementService blockManagement;

  private final AsyncTaskService asyncTaskService;

  private final SubscriptionService subscriptionService;

  public HederaService(
      ContractEventDetailsFactory eventDetailsFactory,
      EventStoreService eventStoreService,
      ObjectMapper objectMapper,
      Node node,
      ScheduledExecutorService scheduledExecutorService,
      ModelMapper modelMapper,
      OkHttpClient okHttpClient,
      EventBlockManagementService blockManagement,
      AsyncTaskService asyncTaskService,
      @Lazy SubscriptionService subscriptionService) {
    this.scheduledExecutorService = scheduledExecutorService;
    this.eventDetailsFactory = eventDetailsFactory;
    this.eventStoreService = eventStoreService;
    this.objectMapper = objectMapper;
    this.okHttpClient = okHttpClient;
    this.modelMapper = modelMapper;
    this.nodeName = node.getName();
    this.nodeUrl = node.getUrl();
    this.nodeHeaders = node.getHeaders();
    this.maxRetries = node.getCallRetries();
    this.nodeLimitPerRequest = node.getLimitPerRequest().toString();
    this.blockManagement = blockManagement;
    this.asyncTaskService = asyncTaskService;
    this.subscriptionService = subscriptionService;
  }

  /**
   * Controls all calls of the class
   *
   * @param request Request to do
   * @param typeReference Type to convert the response to
   * @return Returns an object of typeReference
   */
  private <R> R newCall(Request request, TypeReference<R> typeReference)
      throws IOException, RuntimeException, NotFoundException {
    Response response = this.okHttpClient.newCall(request).execute();
    String message;
    switch (HttpStatus.valueOf(response.code())) {
      case OK:
      case CREATED:
      case ACCEPTED:
      case PARTIAL_CONTENT:
        R responseBody =
            this.objectMapper
                .reader()
                .forType(typeReference)
                .withoutRootName()
                .readValue(response.body().string());
        response.close();
        return responseBody;
      case CONFLICT:
      case BAD_REQUEST:
        message = String.format("Invalid request - %s", request.url().url());
        String bodyText = response.body() != null ? response.body().string() : null;
        boolean responseEntityId =
            bodyText != null && bodyText.toLowerCase().contains("null entity id");
        log.error(message);
        response.close();
        if (responseEntityId) {
          return null;
        } else {
          throw new MirrorUnexpectedException(message);
        }
      case NOT_FOUND:
        message = String.format("Empty response - %s", request.url().url());
        log.trace(message);
        response.close();
        throw new NotFoundException(message);
      default:
        message = String.format("Unexpected response - %s", request.url().url());
        response.close();
        if (response.code() == 429) {
          // If too many request, wait 1 second in order not to saturate
          try {
            log.info("Too many request, waiting for retry...");
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        log.error(message);
        throw new MirrorUnexpectedException(message);
    }
  }

  /**
   * Controls retries of the call to contract results (multiple) It is used from the
   * getContractResultsByBlock method
   *
   * @param httpBuilder Call formed
   * @param tries Number of retries made
   * @param blockNumber Block number to filter
   * @return Returns an object of ContractResultsResponse
   */
  @SneakyThrows
  private ContractResultsResponse doContractResultsRequest(
      HttpUrl.Builder httpBuilder, BigInteger tries, BigInteger blockNumber)
      throws NotFoundException, IOException, MirrorUnexpectedException {
    Request request = generateHttpRequest(httpBuilder);
    ContractResultsResponse response = null;
    try {
      tries = tries.add(BigInteger.ONE);
      response = this.newCall(request, new TypeReference<ContractResultsResponse>() {});

      if (response == null) {
        response = new ContractResultsResponse();
        response.setResults(new ArrayList<ContractResultResponse>());
      }
    } catch (NotFoundException | IOException ignored) {
      log.debug(
          "There was an error getting the results in block number {}, retrying ({})...",
          blockNumber,
          tries);
      Thread.sleep(500);
      if (tries.compareTo(maxRetries) > 0) {
        String errorMsg =
            String.format(
                "Max number of retries exceeded when try to recover contract result (try %s) in block number %s",
                tries, blockNumber);
        log.warn(errorMsg);
        throw new MirrorUnexpectedException(errorMsg);
      } else {
        response = doContractResultsRequest(httpBuilder, tries, blockNumber);
      }
    }
    return response;
  }

  /**
   * Call for contract result (single)
   *
   * @param transactionId Transaction identifier for filter
   * @return Returns an object of ContractResultResponse
   */
  public ContractResultResponse getContractResult(String transactionId)
      throws IOException, NotFoundException {
    String url = nodeUrl + API_VERSION_PATH + CONTRACT_RESULTS_PATH;
    HttpUrl.Builder httpBuilder =
        HttpUrl.parse(String.format("%s/%s", url, transactionId)).newBuilder();
    Request request = generateHttpRequest(httpBuilder);
    return this.newCall(request, new TypeReference<ContractResultResponse>() {});
  }

  /**
   * Gets the contract results of a specific block Controls extra calls if paging exists
   *
   * @param blockNumber Block number for filter
   * @return Returns a list of total contractResults by block number
   */
  public ContractResultsResponse getContractResultsByBlock(BigInteger blockNumber)
      throws IOException, NotFoundException {
    String url = nodeUrl + API_VERSION_PATH + CONTRACT_RESULTS_PATH;
    HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
    httpBuilder.addQueryParameter(BLOCK_NUMBER, blockNumber.toString());
    httpBuilder.addQueryParameter(LIMIT, nodeLimitPerRequest);

    ContractResultsResponse response =
        doContractResultsRequest(httpBuilder, BigInteger.ZERO, blockNumber);

    if (response.getLinks() != null) {
      while (response.getLinks().get(NEXT) != null) {
        httpBuilder = HttpUrl.parse(nodeUrl + response.getLinks().get(NEXT)).newBuilder();
        ContractResultsResponse nestedResponse =
            doContractResultsRequest(httpBuilder, BigInteger.ZERO, blockNumber);

        response.getResults().addAll(nestedResponse.getResults());
        response.setLinks(nestedResponse.getLinks());
      }
    }

    return response;
  }

  /**
   * Controls retries of the call to contract result (single) It is used from the
   * filterAndGetContractResults method
   *
   * @param res Response of the previous response
   * @param tries Number of retries
   * @return Returns a single ContractResultResponse
   */
  private ContractResultResponse callToContractResult(ContractResultResponse res, BigInteger tries)
      throws MirrorUnexpectedException, InterruptedException {
    ContractResultResponse response = null;
    try {
      tries = tries.add(BigInteger.ONE);
      response = this.getContractResult(res.getHash());
    } catch (NotFoundException | IOException err) {
      log.debug(
          "There was an error getting the results of the transaction {}, retrying...",
          res.getHash());
      log.warn("Error ocurring getting the results: ", err);
      Thread.sleep(500);
      if (tries.compareTo(maxRetries) > 0) {
        String errorMsg =
            String.format(
                "Max number of retries exceeded when try to recover contract result of %s",
                res.getHash());
        log.warn(errorMsg);
        throw new MirrorUnexpectedException(errorMsg);
      } else {
        response = callToContractResult(res, tries);
      }
    }
    return response;
  }

  /**
   * Filter and get contract results (multiple)
   *
   * @param responses List of contract results not filtered
   * @return Returns a list of ContractResultResponse
   */
  public List<ContractResultResponse> filterAndGetContractResults(
      List<ContractResultResponse> responses) throws IOException {
    List<ContractResultResponse> txs = new ArrayList<>();
    List<ContractEventFilter> eventFilters = subscriptionService.listContractEventFilters();

    List<ContractResultResponse> responsesFiltered =
        responses.stream()
            .filter(
                response ->
                    eventFilters.stream()
                        .anyMatch(
                            possibleMatch ->
                                possibleMatch
                                        .getContractAddress()
                                        .equalsIgnoreCase(response.getTo())
                                    || possibleMatch
                                        .getContractAddress()
                                        .equalsIgnoreCase(response.getAddress())))
            .toList();

    responsesFiltered.parallelStream()
        .forEach(
            res -> {
              try {
                txs.add(callToContractResult(res, BigInteger.ZERO));
              } catch (MirrorUnexpectedException | InterruptedException ignored) {
              }
            });
    return txs;
  }

  /**
   * Method to manage a block and search for its possible events
   *
   * @param block Block to manage
   */
  public void processBlock(HederaBlock block) {
    block.setNodeName(nodeName);
    String ethTimestamp =
        block.getFromTimestamp().substring(0, block.getFromTimestamp().indexOf("."));
    block.setTimestamp(new BigInteger(ethTimestamp));
    try {
      List<ContractResultResponse> responses =
          this.getContractResultsByBlock(block.getNumber()).getResults();
      log.debug(
          "Contract results: {}, Block Number: {}, From timestamp: {}, To timestamp: {}",
          responses.size(),
          block.getNumber(),
          block.getFromTimestamp(),
          block.getToTimestamp());

      List<ContractResultResponse> responsesFiltered = this.filterAndGetContractResults(responses);

      block.setContractResults(responsesFiltered);
      block.setTransactions(
          responsesFiltered.stream()
              .map(el -> this.modelMapper.map(el, Web3jTransaction.class))
              .collect(Collectors.toList()));
      log.debug(
          "Contract results filtered: {}, Block Number: {}",
          responsesFiltered.size(),
          block.getNumber());
    } catch (NotFoundException | IOException exception) {
      log.warn(exception.getMessage());
    }
  }

  /**
   * This method obtains a block by number
   *
   * @param blockNumber Number of a block
   * @return Returns a Block object
   */
  public BlockResponse getBlock(BigInteger blockNumber) throws IOException, NotFoundException {
    HttpUrl.Builder httpBuilder =
        HttpUrl.parse(nodeUrl + API_VERSION_PATH + BLOCKS_PATH).newBuilder();
    httpBuilder.addQueryParameter(LIMIT, "1");
    httpBuilder.addQueryParameter(BLOCK_NUMBER, blockNumber.toString());

    Request request = generateHttpRequest(httpBuilder);
    BlocksResponse blocksResponse = this.newCall(request, new TypeReference<BlocksResponse>() {});
    if (blocksResponse.getBlocks().isEmpty()) {
      throw new NotFoundException(String.format("Block %d not found!", blockNumber));
    }
    return blocksResponse.getBlocks().getFirst();
  }

  /**
   * Create a flowable object for retrying blocks sequentially
   *
   * @param startBlock Start block for get
   * @param pollingInterval Time between retries
   * @return Returns a flowable HederaBlock
   */
  public Flowable<HederaBlock> blocksFlowable(BigInteger startBlock, Long pollingInterval) {
    AtomicBigInteger currentBlock = new AtomicBigInteger(startBlock);
    return Flowable.create(
        (subscriber) ->
            scheduledExecutorService.scheduleAtFixedRate(
                () -> {
                  try {
                    HederaBlock block =
                        this.modelMapper.map(getBlock(currentBlock.get()), HederaBlock.class);
                    this.processBlock(block);
                    currentBlock.increment();
                    subscriber.onNext(block);
                  } catch (NotFoundException notFoundException) {
                    if (currentBlock.get().compareTo(startBlock) > 0) {
                      log.info(String.format("Awaiting for new block %d", currentBlock.get()));
                    } else {
                      log.error(notFoundException.getMessage());
                    }
                  } catch (Throwable var3) {
                    log.error("Error sending request", var3);
                  }
                },
                0L,
                pollingInterval,
                TimeUnit.MILLISECONDS),
        BackpressureStrategy.DROP);
  }

  /**
   * Obtains the details of a contract event
   *
   * @param filter Filter for event details
   * @param hederaLogResponse Response of log
   * @param contract Contract for event details
   * @return Returns an object of ContractEventDetails
   */
  public ContractEventDetails getEventForFilter(
      ContractEventFilter filter,
      HederaLogResponse hederaLogResponse,
      ContractResultResponse contract) {
    final Log log = buildLog(contract, hederaLogResponse);
    BigInteger timestamp = new BigInteger(contract.getTimestamp().split("\\.")[0]);
    return eventDetailsFactory.createEventDetails(filter, log, timestamp, contract.getFrom());
  }

  /** Build a Log object with a contract object and a log response */
  private Log buildLog(ContractResultResponse contract, HederaLogResponse hederaLogResponse) {
    Log log = new Log();
    log.setAddress(hederaLogResponse.getAddress());
    log.setLogIndex(hederaLogResponse.getIndex());
    log.setTransactionHash(contract.getHash());
    log.setBlockHash(contract.getBlockHash());
    log.setBlockNumber(contract.getBlockNumber());
    log.setData(hederaLogResponse.getData());
    log.setTopics(hederaLogResponse.getTopics());
    return log;
  }

  /** Build a Log object with a log response of mirror node */
  private Log buildLog(LogHederaMirrorNodeResponse logHederaMirrorNodeHederaResponse) {
    Log log = new Log();
    log.setAddress(logHederaMirrorNodeHederaResponse.getAddress());
    log.setLogIndex(logHederaMirrorNodeHederaResponse.getIndex().toString());
    log.setTransactionHash(logHederaMirrorNodeHederaResponse.getTransactionHash());
    log.setBlockHash(logHederaMirrorNodeHederaResponse.getBlockHash());
    log.setBlockNumber(logHederaMirrorNodeHederaResponse.getBlockNumber().toString());
    log.setData(logHederaMirrorNodeHederaResponse.getData());
    log.setTopics(logHederaMirrorNodeHederaResponse.getTopics());
    return log;
  }

  /**
   * Obtains the actual node name configured
   *
   * @return The ethereum node name that this service is connected to.
   */
  @Override
  public String getNodeName() {
    return this.nodeName;
  }

  /**
   * Retrieves all events for a specified event filter.
   *
   * @param eventFilter The contract event filter that should be matched.
   * @param startBlock The start block
   * @param endBlock The end block
   * @return The blockchain contract events
   */
  @Override
  public List<ContractEventDetails> retrieveEvents(
      ContractEventFilter eventFilter, BigInteger startBlock, BigInteger endBlock) {

    final ContractEventSpecification eventSpec = eventFilter.getEventSpecification();

    List<ContractEventDetails> contractEventDetails = new ArrayList<ContractEventDetails>();
    try {
      LogsResponseHederaMirrorNodeResponse logResponse =
          getLogResponse(eventFilter, startBlock, endBlock);
      List<LogHederaMirrorNodeResponse> logsHederaMirrorNode = logResponse.getLogs();
      HttpUrl.Builder httpBuilder;
      while (logResponse.getLinks().getNext() != null) {
        httpBuilder = HttpUrl.parse(nodeUrl + logResponse.getLinks().getNext()).newBuilder();
        Request request = generateHttpRequest(httpBuilder);
        logResponse =
            this.newCall(request, new TypeReference<LogsResponseHederaMirrorNodeResponse>() {});
        logsHederaMirrorNode.addAll(logResponse.getLogs());
      }

      if (eventFilter.getEventSpecification() != null) {
        logsHederaMirrorNode =
            logsHederaMirrorNode.stream()
                .filter(
                    f -> Web3jUtil.getSignature(eventSpec).contentEquals(f.getTopics().getFirst()))
                .collect(Collectors.toList());
      }

      contractEventDetails =
          logsHederaMirrorNode.stream()
              .map(
                  el ->
                      eventDetailsFactory.createEventDetails(
                          eventFilter,
                          buildLog(el),
                          BigInteger.ZERO,
                          eventFilter.getContractAddress()))
              .collect(Collectors.toList());

    } catch (Exception e) {
      throw new BlockchainException("Error when obtaining logs from mirror node", e);
    }
    return contractEventDetails;
  }

  /**
   * Call for obtains a log response
   *
   * @param eventFilter Object for filter the logs
   * @param startBlock Start block for filter
   * @param endBlock End block for filter
   * @return Returns a LogsResponseHederaMirrorNodeResponse object
   */
  private LogsResponseHederaMirrorNodeResponse getLogResponse(
      ContractEventFilter eventFilter, BigInteger startBlock, BigInteger endBlock)
      throws NotFoundException, IOException {
    BlockResponse startBlockInfo = getBlock(startBlock);
    BlockResponse endBlockInfo = getBlock(endBlock);

    HttpUrl.Builder httpBuilder =
        HttpUrl.parse(
                nodeUrl
                    + API_VERSION_PATH
                    + LOGS_PATH.replace(CONTRACT_VAR, eventFilter.getContractAddress()))
            .newBuilder();

    httpBuilder.addQueryParameter(ORDER, ASC);
    httpBuilder.addQueryParameter(LIMIT, nodeLimitPerRequest);
    httpBuilder.addQueryParameter(TIMESTAMP, GTE + ":" + startBlockInfo.getTimestamp().getFrom());
    httpBuilder.addQueryParameter(TIMESTAMP, LTE + ":" + endBlockInfo.getTimestamp().getTo());
    Request request = generateHttpRequest(httpBuilder);
    return this.newCall(request, new TypeReference<LogsResponseHederaMirrorNodeResponse>() {});
  }

  /**
   * Register a contract event listener for the specified event filter, that gets triggered when an
   * event matching the filter is emitted within the Ethereum network.
   *
   * @param eventFilter The contract event filter that should be matched.
   * @param eventListener The listener to be triggered when a matching event is emitted
   * @return The registered subscription
   */
  @Override
  public FilterSubscription registerEventListener(
      ContractEventFilter eventFilter, ContractEventListener eventListener) {
    try {
      final BigInteger startBlock = getStartBlockForEventFilter(eventFilter);
      final BigInteger endBlock =
          eventStoreService
              .getLatestBlock(eventFilter.getNode())
              .orElseThrow(() -> new NotFoundException("Latest block not found"))
              .getNumber();
      Disposable sub =
          createContractEventFlowable(eventFilter, startBlock, endBlock)
              .subscribe(
                  eventListener::onEvent,
                  error -> log.error("Error registering event listener", error));
      if (sub.isDisposed()) {
        // There was an error subscribing
        throw new BlockchainException(
            String.format(
                "Failed to subcribe for filter %s.  The subscription is disposed.",
                eventFilter.getId()));
      }
      return new FilterSubscription(eventFilter, sub, startBlock);
    } catch (NotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @return the current block number of the network that the Ethereum node is connected to.
   */
  @Override
  public BigInteger getCurrentBlockNumber() {
    try {
      HttpUrl.Builder httpBuilder =
          HttpUrl.parse(nodeUrl + API_VERSION_PATH + BLOCKS_PATH).newBuilder();
      httpBuilder.addQueryParameter(LIMIT, "1");
      httpBuilder.addQueryParameter(ORDER, "desc");
      Request request = generateHttpRequest(httpBuilder);
      BlocksResponse blocksResponse = null;
      blocksResponse = this.newCall(request, new TypeReference<BlocksResponse>() {});
      return blocksResponse.getBlocks().getFirst().getNumber();
    } catch (IOException | NotFoundException e) {
      throw new BlockchainException("Error when obtaining the current block number", e);
    }
  }

  /**
   * @return the client version for the connected Ethereum node.
   */
  @Override
  public String getClientVersion() {
    return CLIENT_VERSION;
  }

  /**
   * @param blockHash The hash of the block to obtain
   * @param fullTransactionObjects If full transaction details should be populated
   * @return The block for the specified hash or nothing if a block with the specified hash does not
   *     exist.
   */
  @Override
  public Optional<Block> getBlock(String blockHash, boolean fullTransactionObjects) {
    String url = nodeUrl + API_VERSION_PATH + BLOCKS_PATH;
    HttpUrl.Builder httpBuilder =
        HttpUrl.parse(String.format("%s/%s", url, blockHash)).newBuilder();

    Request request = generateHttpRequest(httpBuilder);
    BlockResponse blockResponse = null;
    try {
      blockResponse = this.newCall(request, new TypeReference<BlockResponse>() {});
      return Optional.of(this.modelMapper.map(blockResponse, HederaBlock.class));
    } catch (IOException | NotFoundException e) {
      return Optional.empty();
    }
  }

  /**
   * Obtain the transaction receipt for a specified transaction id.
   *
   * @param txId the transaction id
   * @return the receipt for the transaction with the specified id.
   */
  @Override
  public TransactionReceipt getTransactionReceipt(String txId) {
    try {
      ContractResultResponse res = this.getContractResult(txId);
      return new HederaTransactionReceipt(res);
    } catch (IOException | NotFoundException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Obtain the latest block for start
   *
   * @param filter Filter
   * @return Returns the number of block to start the obtaining method
   */
  private BigInteger getStartBlockForEventFilter(ContractEventFilter filter) {
    return blockManagement.getLatestBlockForEvent(filter);
  }

  /**
   * Creates flowable of contract event details
   *
   * @param eventFilter Event filter for flowable
   * @param startBlock Start block
   * @param endBlock End block
   * @return Returns flowable of ContractEventDetails
   */
  private Flowable<ContractEventDetails> createContractEventFlowable(
      ContractEventFilter eventFilter, BigInteger startBlock, BigInteger endBlock) {
    return Flowable.create(
        emitter -> {
          asyncTaskService.execute(
              EVENT_EXECUTOR_NAME,
              () -> {
                try {
                  final ContractEventSpecification eventSpec = eventFilter.getEventSpecification();

                  LogsResponseHederaMirrorNodeResponse logResponse =
                      getLogResponse(eventFilter, startBlock, endBlock);
                  HttpUrl.Builder httpBuilder;
                  do {
                    if (eventFilter.getEventSpecification() != null) {
                      logResponse.getLogs().stream()
                          .filter(
                              f ->
                                  Web3jUtil.getSignature(eventSpec)
                                      .contentEquals(f.getTopics().getFirst()))
                          .forEach(
                              it -> {
                                ContractEventDetails details =
                                    eventDetailsFactory.createEventDetails(
                                        eventFilter,
                                        buildLog(it),
                                        BigInteger.ZERO,
                                        eventFilter.getContractAddress());
                                emitter.onNext(details);
                              });
                    }
                    if (logResponse.getLinks().getNext() != null) {
                      httpBuilder =
                          HttpUrl.parse(nodeUrl + logResponse.getLinks().getNext()).newBuilder();
                      Request request = generateHttpRequest(httpBuilder);
                      logResponse =
                          this.newCall(
                              request,
                              new TypeReference<LogsResponseHederaMirrorNodeResponse>() {});
                    }
                  } while (logResponse.getLinks().getNext() != null);

                  emitter.onComplete();
                } catch (IOException | NotFoundException exception) {
                  log.warn(exception.getMessage());
                }
              });
        },
        BackpressureStrategy.BUFFER);
  }

  private Request generateHttpRequest(HttpUrl.Builder httpBuilder) {
    if (nodeHeaders == null || nodeHeaders.isEmpty()) {
      return new Request.Builder().url(httpBuilder.build()).get().build();
    }
    Request.Builder builder = new Request.Builder().url(httpBuilder.build());
    this.nodeHeaders.forEach(builder::addHeader);
    return builder.get().build();
  }

  @Override
  public List<ContractEventDetails> getEventsForFilter(
      ContractEventFilter filter, BigInteger blockNumber) {
    return null;
  }

  @Override
  public String getRevertReason(String from, String to, BigInteger blockNumber, String input) {
    return null;
  }
}
