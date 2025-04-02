package net.consensys.eventeum.plugin.chain.contract;

import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.contract.ContractEventListener;
import net.consensys.eventeum.chain.contract.DefaultContractEventProcessor;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.container.NodeServices;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.TransactionReceipt;
import net.consensys.eventeum.chain.settings.NodeType;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.plugin.chain.service.Web3JEeaService;
import net.consensys.eventeum.service.AsyncTaskService;
import net.consensys.eventeum.utils.ExecutorNameFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Primary
public class EeaContractEventProcessor extends DefaultContractEventProcessor {

  private final AsyncTaskService asyncTaskService;

  public EeaContractEventProcessor(
      ChainServicesContainer chainServices,
      AsyncTaskService asyncTaskService,
      List<ContractEventListener> contractEventListeners) {
    super(chainServices, asyncTaskService, contractEventListeners);
    this.asyncTaskService = asyncTaskService;
  }

  @Override
  public void processLogsInBlock(Block block, List<ContractEventFilter> contractEventFilters) {
    asyncTaskService
        .executeWithCompletableFuture(
            ExecutorNameFactory.build(EVENT_EXECUTOR_NAME, block.getNodeName()),
            () -> {
              final NodeServices nodeServices = chainServices.getNodeServices(block.getNodeName());
              switch (NodeType.valueOf(nodeServices.getNodeType())) {
                case MIRROR:
                  this.processLogsInMirrorNodeBlock(block, contractEventFilters);
                  break;
                case NORMAL:
                default:
                  this.processLogsInNormalNodeBlock(block, contractEventFilters);
                  break;
              }
            })
        .join();
  }

  private void processLogsInNormalNodeBlock(
      Block block, List<ContractEventFilter> contractEventFilters) {
    final BlockchainService blockchainService = getBlockchainService(block.getNodeName());
    final Web3JEeaService web3JEeaService = (Web3JEeaService) blockchainService;

    final List<String> privateTransactionsLogsBloom =
        block.getTransactions().stream()
            .filter(web3JEeaService::isPrivateTransaction)
            .map(tx -> web3JEeaService.getPrivateTransactionReceipt(tx.getHash()))
            .filter(Objects::nonNull)
            .map(TransactionReceipt::getLogsBloom)
            .toList();

    contractEventFilters.forEach(
        filter -> {
          if (!block.getTransactions().isEmpty()
              && block.getNodeName().equals(filter.getNode())
              && ((privateTransactionsLogsBloom.stream()
                      .anyMatch(logsBloom -> isEventFilterInBloomFilter(filter, logsBloom)))
                  || isEventFilterInBloomFilter(filter, block.getLogsBloom()))) {
            processWeb3LogsForFilter(filter, block, blockchainService);
          }
        });
  }

  private void processWeb3LogsForFilter(
      ContractEventFilter filter, Block block, BlockchainService blockchainService) {

    blockchainService
        .getEventsForFilter(filter, block.getNumber())
        .forEach(
            event -> {
              event.setTimestamp(block.getTimestamp());
              super.triggerListeners(event);
            });
  }
}
