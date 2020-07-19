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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.util.BloomFilterUtil;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.service.AsyncTaskService;
import net.consensys.eventeum.utils.ExecutorNameFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class DefaultContractEventProcessor implements ContractEventProcessor {

    private static final String EVENT_EXECUTOR_NAME = "EVENT";

    private ChainServicesContainer chainServices;

    private AsyncTaskService asyncTaskService;

    private List<ContractEventListener> contractEventListeners;

    @Override
    public void processLogsInBlock(Block block, List<ContractEventFilter> contractEventFilters) {
        asyncTaskService.executeWithCompletableFuture(ExecutorNameFactory.build(EVENT_EXECUTOR_NAME, block.getNodeName()), () -> {
            final BlockchainService blockchainService = getBlockchainService(block.getNodeName());

            contractEventFilters
                    .forEach(filter -> processLogsForFilter(filter, block, blockchainService));
        }).join();
    }

    @Override
    public void processContractEvent(ContractEventDetails contractEventDetails) {
        asyncTaskService.executeWithCompletableFuture(
                ExecutorNameFactory.build(EVENT_EXECUTOR_NAME, contractEventDetails.getNodeName()),
                () -> triggerListeners(contractEventDetails)).join();
    }

    private void processLogsForFilter(ContractEventFilter filter,
                                      Block block,
                                      BlockchainService blockchainService) {

        if (block.getNodeName().equals(filter.getNode())
                && isEventFilterInBloomFilter(filter, block.getLogsBloom())) {
            blockchainService
                    .getEventsForFilter(filter, block.getNumber())
                    .forEach(event -> {
                        event.setTimestamp(block.getTimestamp());
                        triggerListeners(event);
                    });
        }
    }

    private boolean isEventFilterInBloomFilter(ContractEventFilter filter, String logsBloom) {
        final BloomFilterUtil.BloomFilterBits bloomBits = BloomFilterUtil.getBloomBits(filter);

        return BloomFilterUtil.bloomFilterMatch(logsBloom, bloomBits);
    }

    private BlockchainService getBlockchainService(String nodeName) {
        return chainServices.getNodeServices(nodeName).getBlockchainService();
    }

    private void triggerListeners(ContractEventDetails contractEvent) {
        contractEventListeners.forEach(
                listener -> triggerListener(listener, contractEvent));
    }

    private void triggerListener(ContractEventListener listener, ContractEventDetails contractEventDetails) {
        try {
            listener.onEvent(contractEventDetails);
        } catch (Throwable t) {
            log.error(String.format(
                    "An error occurred when processing contractEvent with id %s", contractEventDetails.getId()), t);
            throw t;
        }
    }
}
