package net.consensys.eventeum.chain.service.strategy;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.service.AsyncTaskService;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import rx.Subscription;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PollingBlockSubscriptionStrategy extends AbstractBlockSubscriptionStrategy<EthBlock> {

    public PollingBlockSubscriptionStrategy(Web3j web3j) {
        super(web3j);
    }

    @Override
    public Subscription subscribe() {
        blockSubscription = web3j.blockObservable(false).subscribe(block -> {
            triggerListeners(block);
        });

        return blockSubscription;
    }

    @Override
    BlockDetails convertToBlockDetails(EthBlock blockObject) {
        final EthBlock.Block block = blockObject.getBlock();
        final BlockDetails blockDetails = new BlockDetails();

        blockDetails.setNumber(block.getNumber());
        blockDetails.setHash(block.getHash());
        blockDetails.setTimestamp(block.getTimestamp());

        return blockDetails;
    }
}
