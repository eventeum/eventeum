package net.consensys.eventeum.chain.service.strategy;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.service.AsyncTaskService;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import rx.Subscription;

public class PollingBlockSubscriptionStrategy extends AbstractBlockSubscriptionStrategy {

    public PollingBlockSubscriptionStrategy(Web3j web3j, AsyncTaskService asyncTaskService) {
        super(web3j, asyncTaskService);
    }

    @Override
    public Subscription subscribe() {
        blockSubscription = web3j.blockObservable(false).subscribe(block -> {
            blockListeners.forEach(listener ->
                    asyncTaskService.execute(() -> listener.onBlock(blockToBlockDetails(block))));
        });

        return blockSubscription;
    }

    private BlockDetails blockToBlockDetails(EthBlock ethBlock) {
        final EthBlock.Block block = ethBlock.getBlock();
        final BlockDetails blockDetails = new BlockDetails();

        blockDetails.setNumber(block.getNumber());
        blockDetails.setHash(block.getHash());

        return blockDetails;
    }
}
