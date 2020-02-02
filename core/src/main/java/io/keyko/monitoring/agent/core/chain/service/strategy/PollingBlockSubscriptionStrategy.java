package io.keyko.monitoring.agent.core.chain.service.strategy;

import io.keyko.monitoring.agent.core.chain.service.domain.wrapper.Web3jBlock;
import io.keyko.monitoring.agent.core.model.LatestBlock;
import io.reactivex.disposables.Disposable;
import io.keyko.monitoring.agent.core.chain.service.domain.Block;
import io.keyko.monitoring.agent.core.service.AsyncTaskService;
import io.keyko.monitoring.agent.core.service.EventStoreService;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.util.Optional;

public class PollingBlockSubscriptionStrategy extends AbstractBlockSubscriptionStrategy<EthBlock> {

    public PollingBlockSubscriptionStrategy(
            Web3j web3j, String nodeName, EventStoreService eventStoreService, AsyncTaskService asyncService) {
        super(web3j, nodeName, eventStoreService, asyncService);
    }

    @Override
    public Disposable subscribe() {

        final Optional<LatestBlock> latestBlock = getLatestBlock();

        if (latestBlock.isPresent()) {
            final DefaultBlockParameter blockParam = DefaultBlockParameter.valueOf(latestBlock.get().getNumber());

            blockSubscription = web3j.replayPastAndFutureBlocksFlowable(blockParam, true)
                    .subscribe(block -> {
                        triggerListeners(block);
                    });

        } else {
            blockSubscription = web3j.blockFlowable(true).subscribe(block -> {
                triggerListeners(block);
            });
        }

        return blockSubscription;
    }

    @Override
    Block convertToEventeumBlock(EthBlock blockObject) {
        return new Web3jBlock(blockObject.getBlock(), nodeName);
    }
}
