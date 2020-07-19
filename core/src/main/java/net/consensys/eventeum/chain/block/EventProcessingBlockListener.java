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

package net.consensys.eventeum.chain.block;

import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.contract.ContractEventProcessor;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@Slf4j
public class EventProcessingBlockListener implements BlockListener {

    @Lazy
    private SubscriptionService subscriptionService;

    private ContractEventProcessor contractEventProcessor;

    @Autowired
    public EventProcessingBlockListener(@Lazy SubscriptionService subscriptionService,
                                        ContractEventProcessor contractEventProcessor) {
        this.subscriptionService = subscriptionService;
        this.contractEventProcessor = contractEventProcessor;
    }

    @Override
    public void onBlock(Block block) {
        contractEventProcessor.processLogsInBlock(block, subscriptionService.listContractEventFilters());
    }
}
