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

package net.consensys.eventeum.chain.service;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.contract.ContractEventListener;
import net.consensys.eventeum.chain.factory.ContractEventDetailsFactory;
import net.consensys.eventeum.chain.service.block.EventBlockManagementService;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.TransactionReceipt;
import net.consensys.eventeum.chain.service.domain.wrapper.Web3jBlock;
import net.consensys.eventeum.chain.service.domain.wrapper.Web3jTransactionReceipt;
import net.consensys.eventeum.chain.util.Web3jUtil;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.model.FilterSubscription;
import net.consensys.eventeum.service.AsyncTaskService;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;

/**
 * A BlockchainService implementating utilising the Web3j library.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Slf4j
public class Web3jService implements BlockchainService {

  private static final String EVENT_EXECUTOR_NAME = "EVENT";

  @Getter @Setter private Web3j web3j;
  private final String nodeName;
  private final AsyncTaskService asyncTaskService;
  private final EventBlockManagementService blockManagement;
  private final ContractEventDetailsFactory eventDetailsFactory;

  public Web3jService(
      String nodeName,
      Web3j web3j,
      ContractEventDetailsFactory eventDetailsFactory,
      AsyncTaskService asyncTaskService,
      EventBlockManagementService blockManagement) {
    this.web3j = web3j;
    this.nodeName = nodeName;
    this.asyncTaskService = asyncTaskService;
    this.blockManagement = blockManagement;
    this.eventDetailsFactory = eventDetailsFactory;
  }

  @Override
  public String getNodeName() {
    return this.nodeName;
  }

  @Override
  public List<ContractEventDetails> retrieveEvents(
      ContractEventFilter eventFilter, BigInteger startBlock, BigInteger endBlock) {
    final ContractEventSpecification eventSpec = eventFilter.getEventSpecification();

    final EthFilter ethFilter =
        new EthFilter(
            new DefaultBlockParameterNumber(startBlock),
            new DefaultBlockParameterNumber(endBlock),
            eventFilter.getContractAddress());

    if (eventFilter.getEventSpecification() != null) {
      ethFilter.addSingleTopic(Web3jUtil.getSignature(eventSpec));
    }

    try {
      return extractEventDetailsFromLogs(ethFilter, eventFilter, endBlock);
    } catch (IOException e) {
      throw new BlockchainException("Error obtaining logs", e);
    }
  }

  /** {inheritDoc} */
  @Override
  public FilterSubscription registerEventListener(
      ContractEventFilter eventFilter, ContractEventListener eventListener) {

    log.debug("Registering event filter for event: {}", eventFilter.getId());
    final ContractEventSpecification eventSpec = eventFilter.getEventSpecification();

    final BigInteger startBlock = getStartBlockForEventFilter(eventFilter);

    final EthFilter ethFilter =
        new EthFilter(
            new DefaultBlockParameterNumber(startBlock),
            DefaultBlockParameterName.LATEST,
            eventFilter.getContractAddress());

    if (eventFilter.getEventSpecification() != null) {
      ethFilter.addSingleTopic(Web3jUtil.getSignature(eventSpec));
    }

    final Flowable<Log> flowable = createEthLogFlowable(ethFilter, eventFilter, null);

    final Disposable sub =
        flowable.subscribe(
            theLog -> {
              if (theLog != null) {
                asyncTaskService.execute(
                    EVENT_EXECUTOR_NAME,
                    () -> {
                      try {
                        BigInteger blockNumber = theLog.getBlockNumber();
                        DefaultBlockParameterNumber blockParameterNumber =
                            new DefaultBlockParameterNumber(blockNumber);
                        EthBlock ethBlock =
                            this.web3j.ethGetBlockByNumber(blockParameterNumber, false).send();
                        TransactionReceipt transactionReceipt = getTransactionReceipt(theLog);

                        log.debug("Dispatching log: {}", theLog);
                        eventListener.onEvent(
                            eventDetailsFactory.createEventDetails(
                                eventFilter, theLog, ethBlock, transactionReceipt));
                      } catch (IOException exception) {
                        log.warn(exception.getMessage());
                      }
                    });
              } else {
                log.info("Null log event");
              }
            },
            error -> log.error("Error registering event listener", error));

    if (sub.isDisposed()) {
      // There was an error subscribing
      throw new BlockchainException(
          String.format(
              "Failed to subcribe for filter %s.  The subscription is disposed.",
              eventFilter.getId()));
    }

    return new FilterSubscription(eventFilter, sub, startBlock);
  }

  private TransactionReceipt getTransactionReceipt(Log theLog) {
    TransactionReceipt transactionReceipt = null;

    while (transactionReceipt == null) {
      try {
        transactionReceipt = this.getTransactionReceipt(theLog.getTransactionHash());
      } catch (BlockchainException blockchainException) {
        log.debug(
            "Retrying to recover transaction receipt of {} because '{}'",
            theLog.getTransactionHash(),
            blockchainException.getMessage());
      }
    }

    return transactionReceipt;
  }

  /** {inheritDoc} */
  @Override
  public String getClientVersion() {
    try {
      final Web3ClientVersion web3ClientVersion = web3j.web3ClientVersion().send();
      return web3ClientVersion.getWeb3ClientVersion();
    } catch (IOException e) {
      throw new BlockchainException("Error when obtaining client version", e);
    }
  }

  /** {inheritDoc} */
  @Override
  public TransactionReceipt getTransactionReceipt(String txId) { // Overwritten in eea
    try {
      final EthGetTransactionReceipt response = web3j.ethGetTransactionReceipt(txId).send();

      return response
          .getTransactionReceipt()
          .map(receipt -> new Web3jTransactionReceipt(receipt))
          .orElse(null);
    } catch (IOException e) {
      throw new BlockchainException("Unable to connect to the ethereum client", e);
    }
  }

  /** {inheritDoc} */
  @Override
  public BigInteger getCurrentBlockNumber() {
    try {
      final EthBlockNumber ethBlockNumber = web3j.ethBlockNumber().send();

      return ethBlockNumber.getBlockNumber();
    } catch (IOException e) {
      throw new BlockchainException("Error when obtaining the current block number", e);
    }
  }

  public Optional<Block> getBlock(String blockHash, boolean fullTransactionObjects) {
    try {
      final EthBlock blockResponse =
          web3j.ethGetBlockByHash(blockHash, fullTransactionObjects).send();

      if (blockResponse.getBlock() == null) {
        return Optional.empty();
      }

      return Optional.of(new Web3jBlock(blockResponse.getBlock(), nodeName));
    } catch (IOException e) {
      throw new BlockchainException("Error when obtaining block with hash: " + blockHash, e);
    }
  }

  public List<ContractEventDetails> getEventsForFilter(
      ContractEventFilter filter, BigInteger blockNumber) {

    try {
      final ContractEventSpecification eventSpec = filter.getEventSpecification();
      EthFilter ethFilter =
          new EthFilter(
              new DefaultBlockParameterNumber(blockNumber),
              new DefaultBlockParameterNumber(blockNumber),
              filter.getContractAddress());

      if (filter.getEventSpecification() != null) {
        ethFilter = ethFilter.addSingleTopic(Web3jUtil.getSignature(eventSpec));
      }

      return extractEventDetailsFromLogs(ethFilter, filter, blockNumber);
    } catch (IOException e) {
      throw new BlockchainException("Error when obtaining logs from block: " + blockNumber, e);
    }
  }

  @Override
  public String getRevertReason(String from, String to, BigInteger blockNumber, String input) {
    try {
      return web3j
          .ethCall(
              Transaction.createEthCallTransaction(from, to, input),
              DefaultBlockParameter.valueOf(blockNumber))
          .send()
          .getRevertReason();
    } catch (IOException e) {
      throw new BlockchainException("Error getting the revert reason", e);
    }
  }

  protected List<ContractEventDetails> extractEventDetailsFromLogs(
      EthFilter ethFilter, ContractEventFilter eventFilter, BigInteger blockNumber)
      throws IOException {

    final EthLog logs = web3j.ethGetLogs(ethFilter).send();
    DefaultBlockParameterNumber blockParameterNumber = new DefaultBlockParameterNumber(blockNumber);
    EthBlock ethBlock = this.web3j.ethGetBlockByNumber(blockParameterNumber, false).send();

    return logs.getLogs().stream()
        .map(
            logResult ->
                eventDetailsFactory.createEventDetails(
                    eventFilter,
                    (Log) logResult.get(),
                    ethBlock,
                    getTransactionReceipt((Log) logResult.get())))
        .collect(Collectors.toList());
  }

  protected Flowable<Log> createEthLogFlowable(
      EthFilter ethFilter, ContractEventFilter eventFilter, Optional<Runnable> onCompletion) {
    log.debug("Creating EthLog flowable for filter {}", eventFilter.getId());
    return web3j
        .ethLogFlowable(ethFilter)
        .doOnComplete(
            () -> {
              if (onCompletion.isPresent()) {
                onCompletion.get().run();
              }
            });
  }

  private BigInteger getStartBlockForEventFilter(ContractEventFilter filter) {
    return blockManagement.getLatestBlockForEvent(filter);
  }
}
