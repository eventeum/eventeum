package net.consensys.eventeum.chain.block;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.contract.ContractEventListener;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.util.BloomFilterUtil;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.service.AsyncTaskService;
import net.consensys.eventeum.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;

@Component
@Slf4j
public class EventProcessingBlockListener implements BlockListener {

    private static final String EVENT_EXECUTOR_NAME = "EVENT";

    @Lazy
    private SubscriptionService subscriptionService;

    private ChainServicesContainer chainServices;

    private AsyncTaskService asyncTaskService;

    private List<ContractEventListener> contractEventListeners;

    @Autowired
    public EventProcessingBlockListener(@Lazy SubscriptionService subscriptionService,
                                        ChainServicesContainer chainServices,
                                        AsyncTaskService asyncTaskService,
                                        List<ContractEventListener> contractEventListeners) {
        this.subscriptionService = subscriptionService;
        this.chainServices = chainServices;
        this.asyncTaskService = asyncTaskService;
        this.contractEventListeners = contractEventListeners;
    }

    @Override
    public void onBlock(Block block) {
        asyncTaskService.execute(EVENT_EXECUTOR_NAME, () -> {
            final BlockchainService blockchainService = getBlockchainService(block.getNodeName());

            subscriptionService.listContractEventFilters()
                    .forEach(filter -> processLogsForFilter(filter, block, blockchainService));
        });
    }

    private void processLogsForFilter(ContractEventFilter filter,
                                      Block block,
                                      BlockchainService blockchainService) {

        if (isEventFilterInBloomFilter(filter, block.getLogsBloom())) {
            blockchainService
                    .getEventsForFilter(filter, block.getNumber())
                    .forEach(event -> triggerListeners(event));
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
        }
    }
}
