package net.consensys.eventeum.chain.block;

import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.contract.ContractEventProcessor;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

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
