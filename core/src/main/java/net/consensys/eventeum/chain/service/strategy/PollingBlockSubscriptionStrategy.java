package net.consensys.eventeum.chain.service.strategy;

import io.reactivex.disposables.Disposable;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.wrapper.Web3jBlock;
import net.consensys.eventeum.model.LatestBlock;
import net.consensys.eventeum.service.AsyncTaskService;
import net.consensys.eventeum.service.EventStoreService;
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
