package net.consensys.eventeum.plugin.chain.service;

import io.reactivex.Flowable;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.factory.ContractEventDetailsFactory;
import net.consensys.eventeum.chain.service.BlockchainException;
import net.consensys.eventeum.chain.service.Web3jService;
import net.consensys.eventeum.chain.service.block.EventBlockManagementService;
import net.consensys.eventeum.chain.service.domain.Transaction;
import net.consensys.eventeum.chain.service.domain.TransactionReceipt;
import net.consensys.eventeum.chain.service.domain.wrapper.Web3jTransactionReceipt;
import net.consensys.eventeum.chain.settings.NodeSettings;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.plugin.chain.model.PrivacyConfFilter;
import net.consensys.eventeum.plugin.chain.model.PrivacyConfNode;
import net.consensys.eventeum.plugin.chain.util.PrivacyUtils;
import net.consensys.eventeum.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.besu.Besu;
import org.web3j.protocol.besu.response.privacy.PrivGetPrivateTransaction;
import org.web3j.protocol.besu.response.privacy.PrivGetTransactionReceipt;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.utils.Async;

@Slf4j
public class Web3JEeaService extends Web3jService {

  private final Web3j web3j;
  private final ContractEventDetailsFactory eventDetailsFactory;
  private Besu web3JEea;
  @Autowired private NodeSettings nodeSettings;
  private PrivacyConfNode privacyConf;

  public Web3JEeaService(
      String nodeName,
      Web3j web3j,
      ContractEventDetailsFactory eventDetailsFactory,
      AsyncTaskService asyncTaskService,
      EventBlockManagementService blockManagement) {

    super(nodeName, web3j, eventDetailsFactory, asyncTaskService, blockManagement);
    this.web3j = web3j;
    this.eventDetailsFactory = eventDetailsFactory;
  }

  @PostConstruct
  private void init() throws NoSuchFieldException, IllegalAccessException {
    web3JEea =
        Besu.build(
            getNetworkServiceFromWeb3jInstance(web3j),
            nodeSettings.getNode(getNodeName()).getPollingInterval(),
            Async.defaultExecutorService());
    privacyConf =
        PrivacyUtils.buildPrivacyConfNodeFromExtension(
            nodeSettings.getNodes().get(getNodeName()).getExtension());
  }

  @Override
  protected List<ContractEventDetails> extractEventDetailsFromLogs(
      EthFilter ethFilter, ContractEventFilter eventFilter, BigInteger blockNumber)
      throws IOException {

    PrivacyConfFilter privData =
        PrivacyUtils.buildPrivacyConfFilterFromExtension(eventFilter.getExtension());

    if (privData != null && privData.isEnabled() && isPrivacyEnabled()) {
      final EthLog logs = web3JEea.privGetLogs(privData.getPrivacyGroupID(), ethFilter).send();
      DefaultBlockParameterNumber blockParameterNumber =
          new DefaultBlockParameterNumber(blockNumber);
      EthBlock ethBlock = this.web3j.ethGetBlockByNumber(blockParameterNumber, false).send();
      return logs.getLogs().stream()
          .map(
              logResult ->
                  eventDetailsFactory.createEventDetails(
                      eventFilter,
                      (Log) logResult.get(),
                      ethBlock,
                      getTransactionReceipt(((Log) logResult.get()).getTransactionHash())))
          .collect(Collectors.toList());
    }

    return super.extractEventDetailsFromLogs(ethFilter, eventFilter, blockNumber);
  }

  /** {inheritDoc} */
  @Override
  public TransactionReceipt getTransactionReceipt(String txHash) {
    log.debug("Getting tx recepit");
    if (isPrivateTransaction(txHash)) {
      log.debug("Receipt is private");
      return getPrivateTransactionReceipt(txHash);
    } else {
      return super.getTransactionReceipt(txHash);
    }
  }

  /** {inheritDoc} */
  @Override
  protected Flowable<Log> createEthLogFlowable(
      EthFilter ethFilter, ContractEventFilter eventFilter, Optional<Runnable> onCompletion) {
    PrivacyConfFilter privData =
        PrivacyUtils.buildPrivacyConfFilterFromExtension(eventFilter.getExtension());

    if (privData != null && privData.isEnabled() && isPrivacyEnabled()) {
      log.debug("Creating private EthLog flowable for filter {}", eventFilter.getId());
      return web3JEea
          .privLogFlowable(privData.getPrivacyGroupID(), ethFilter)
          .doOnComplete(
              () -> {
                if (onCompletion.isPresent()) {
                  onCompletion.get().run();
                }
              });
    }

    return super.createEthLogFlowable(ethFilter, eventFilter, onCompletion);
  }

  public boolean isPrivacyEnabled() {
    return privacyConf != null && privacyConf.isEnabled();
  }

  public boolean isPrivateTransaction(String txHash) {
    final TransactionReceipt publicTx = super.getTransactionReceipt(txHash);
    return isPrivacyEnabled()
        && publicTx != null
        && publicTx.getTo() != null
        && publicTx.getTo().equals(privacyConf.getPrivacyPrecompiledAddress());
  }

  public boolean isPrivateTransaction(Transaction tx) {
    return isPrivacyEnabled()
        && tx.getTo() != null
        && tx.getTo().equals(privacyConf.getPrivacyPrecompiledAddress());
  }

  public Transaction getPrivateTransaction(String txHash) throws IOException {
    PrivGetPrivateTransaction privateTx =
        web3JEea.privGetPrivateTransaction(txHash).send(); // TODO returning null tx
    return privateTx
        .getPrivateTransaction()
        .map(PrivacyUtils::buildTransactionFromPrivateTransaction)
        .orElse(null);
  }

  public TransactionReceipt getPrivateTransactionReceipt(String txHash) {
    try {
      PrivGetTransactionReceipt privateTx = web3JEea.privGetTransactionReceipt(txHash).send();

      return privateTx
          .getTransactionReceipt()
          .map(
              (receipt) -> {
                // Node returns null for cumulativeGasUsed and gasUsed
                // so we have to set 0 value to avoid null pointers
                // in other parts of the code
                receipt.setCumulativeGasUsed("0");
                receipt.setGasUsed("0");
                return new Web3jTransactionReceipt(receipt);
              })
          .orElse(null);
    } catch (IOException e) {
      throw new BlockchainException("Unable to connect to the ethereum client", e);
    }
  }

  /////////////////////////////////////////////////////
  /////////////////// PRIVATE METHODS///////////////////
  /////////////////////////////////////////////////////

  private org.web3j.protocol.Web3jService getNetworkServiceFromWeb3jInstance(Web3j web3j)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = web3j.getClass().getDeclaredField("web3jService");
    field.setAccessible(true);
    return (org.web3j.protocol.Web3jService) field.get(web3j);
  }
}
