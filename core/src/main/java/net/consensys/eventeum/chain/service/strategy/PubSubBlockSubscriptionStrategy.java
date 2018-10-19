package net.consensys.eventeum.chain.service.strategy;

import net.consensys.eventeum.dto.block.BlockDetails;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.websocket.events.NewHead;
import org.web3j.utils.Numeric;
import rx.Subscription;

import java.math.BigInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PubSubBlockSubscriptionStrategy extends AbstractBlockSubscriptionStrategy<NewHead> {

    private Lock lock = new ReentrantLock();

    public PubSubBlockSubscriptionStrategy(Web3j web3j) {
        super(web3j);
    }

    @Override
    public Subscription subscribe() {
        blockSubscription = web3j.newHeadsNotifications().subscribe(newHead -> {
            triggerListeners(newHead.getParams().getResult());
        });

        return blockSubscription;
    }

    @Override
    BlockDetails convertToBlockDetails(NewHead blockObject) {
        final BlockDetails blockDetails = new BlockDetails();
        blockDetails.setHash(blockObject.getHash());
        blockDetails.setNumber(Numeric.decodeQuantity(blockObject.getNumber()));
        blockDetails.setTimestamp(new BigInteger(blockObject.getTimestamp()));

        return blockDetails;
    }
}
