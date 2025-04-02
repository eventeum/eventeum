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

package net.consensys.eventeum.chain.contract;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.HederaService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.container.NodeServices;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.io.ContractResultResponse;
import net.consensys.eventeum.chain.service.domain.io.HederaLogResponse;
import net.consensys.eventeum.chain.service.domain.wrapper.HederaBlock;
import net.consensys.eventeum.chain.settings.NodeType;
import net.consensys.eventeum.chain.util.BloomFilterUtil;
import net.consensys.eventeum.chain.util.Web3jUtil;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.service.AsyncTaskService;
import net.consensys.eventeum.utils.ExecutorNameFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class DefaultContractEventProcessor implements ContractEventProcessor {

  public static final String EVENT_EXECUTOR_NAME = "EVENT";

  protected ChainServicesContainer chainServices;

  private AsyncTaskService asyncTaskService;

  private List<ContractEventListener> contractEventListeners;

  @Override
  public void processLogsInBlock(Block block, List<ContractEventFilter> contractEventFilters) {
    asyncTaskService
        .executeWithCompletableFuture(
            ExecutorNameFactory.build(EVENT_EXECUTOR_NAME, block.getNodeName()),
            () -> {
              final NodeServices nodeServices =
                  this.chainServices.getNodeServices(block.getNodeName());
              switch (NodeType.valueOf(nodeServices.getNodeType())) {
                case MIRROR:
                  this.processLogsInMirrorNodeBlock(block, contractEventFilters);
                  break;
                case NORMAL:
                  contractEventFilters.forEach(
                      filter ->
                          processLogsForFilter(filter, block, nodeServices.getBlockchainService()));
                  break;
              }
            })
        .join();
  }

  @Override
  public void processContractEvent(ContractEventDetails contractEventDetails) {
    asyncTaskService
        .executeWithCompletableFuture(
            ExecutorNameFactory.build(EVENT_EXECUTOR_NAME, contractEventDetails.getNodeName()),
            () -> triggerListeners(contractEventDetails))
        .join();
  }

  protected boolean isEventFilterInBloomFilter(ContractEventFilter filter, String logsBloom) {
    final BloomFilterUtil.BloomFilterBits bloomBits = BloomFilterUtil.getBloomBits(filter);

    return BloomFilterUtil.bloomFilterMatch(logsBloom, bloomBits);
  }

  protected BlockchainService getBlockchainService(String nodeName) {
    return chainServices.getNodeServices(nodeName).getBlockchainService();
  }

  protected HederaService getHederaService(String nodeName) {
    return chainServices.getNodeServices(nodeName).getHederaService();
  }

  protected void processLogsInMirrorNodeBlock(
      Block block, List<ContractEventFilter> contractEventFilters) {
    final HederaService hederaService = getHederaService(block.getNodeName());
    List<ContractResultResponse> contractResultResponseList =
        ((HederaBlock) block).getContractResults();
    if (contractResultResponseList != null && !contractResultResponseList.isEmpty()) {
      contractResultResponseList.forEach(
          res -> {
            processTransactionData(hederaService, res, contractEventFilters);
          });
    }
  }

  protected void triggerListeners(ContractEventDetails contractEvent) {
    contractEventListeners.forEach(listener -> triggerListener(listener, contractEvent));
  }

  private void processLogsForFilter(
      ContractEventFilter filter, Block block, BlockchainService blockchainService) {

    if (block.getNodeName().equals(filter.getNode())
        && isEventFilterInBloomFilter(filter, block.getLogsBloom())) {
      blockchainService
          .getEventsForFilter(filter, block.getNumber())
          .forEach(
              event -> {
                event.setTimestamp(block.getTimestamp());
                event.setBlockTimestamp(block.getTimestamp());
                triggerListeners(event);
              });
    }
  }

  private void triggerListener(
      ContractEventListener listener, ContractEventDetails contractEventDetails) {
    try {
      listener.onEvent(contractEventDetails);
    } catch (Throwable t) {
      log.error(
          String.format(
              "An error occurred when processing contractEvent with id %s",
              contractEventDetails.getEventIdentifier()),
          t);
      throw t;
    }
  }

  private void processMirrorLogsForFilter(
      ContractEventFilter filter,
      HederaService hederaService,
      HederaLogResponse hederaLogResponse,
      ContractResultResponse contractResult) {
    triggerListeners(hederaService.getEventForFilter(filter, hederaLogResponse, contractResult));
  }

  private void processTransactionData(
      HederaService hederaService,
      ContractResultResponse contractResult,
      List<ContractEventFilter> contractEventFilters) {
    contractResult
        .getLogs()
        .forEach(
            hederaLogResponse ->
                contractEventFilters.forEach(
                    filter -> {
                      if (isEventFilterInTopic(hederaLogResponse, filter)
                          && isContractFromEventFilter(hederaLogResponse, filter)) {
                        processMirrorLogsForFilter(
                            filter, hederaService, hederaLogResponse, contractResult);
                      }
                    }));
  }

  private boolean isEventFilterInTopic(HederaLogResponse log, ContractEventFilter filter) {
    String eventSignature = Web3jUtil.getSignature(filter.getEventSpecification());
    return log.getTopics().get(0).equals(eventSignature);
  }

  private boolean isContractFromEventFilter(HederaLogResponse log, ContractEventFilter filter) {
    return filter.getContractAddress().equalsIgnoreCase(log.getAddress());
  }
}
