package net.consensys.eventeum.chain.service.strategy;

import io.reactivex.disposables.Disposable;
import lombok.Data;
import lombok.Setter;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.integration.eventstore.EventStore;
import net.consensys.eventeum.model.LatestBlock;
import net.consensys.eventeum.service.EventStoreService;
import net.consensys.eventeum.utils.JSON;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.websocket.events.NewHead;
import org.web3j.utils.Numeric;
import rx.Observable;
import rx.Subscription;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PubSubBlockSubscriptionStrategy extends AbstractBlockSubscriptionStrategy<NewHead> {

    private Lock lock = new ReentrantLock();

    public PubSubBlockSubscriptionStrategy(Web3j web3j, String nodeName, EventStoreService eventStoreService) {
        super(web3j, nodeName, eventStoreService);
    }

    @Override
    public Disposable subscribe() {
        final Optional<LatestBlock> latestBlock = getLatestBlock();

        if (latestBlock.isPresent()) {
            final DefaultBlockParameter blockParam = DefaultBlockParameter.valueOf(latestBlock.get().getNumber());

            //New heads can only start from latest block so we need to obtain missing blocks first
            web3j.replayPastAndFutureBlocksFlowable(blockParam, false)
                    .doOnComplete(() -> blockSubscription = subscribeToNewHeads())
                    .subscribe(ethBlock -> triggerListeners(convertToNewHead(ethBlock)));
        } else {
            blockSubscription = subscribeToNewHeads();
        }

        return blockSubscription;
    }

    private Disposable subscribeToNewHeads() {
        return web3j.newHeadsNotifications().subscribe(newHead -> {
            triggerListeners(newHead.getParams().getResult());
        });
    }

    NewHead convertToNewHead(EthBlock ethBlock) {
        final BasicNewHead newHead = new BasicNewHead();
        newHead.setHash(ethBlock.getBlock().getHash());
        newHead.setNumber(ethBlock.getBlock().getNumberRaw());
        newHead.setTimestamp(ethBlock.getBlock().getTimestampRaw());

        return newHead;
    }

    @Override
    BlockDetails convertToBlockDetails(NewHead blockObject) {
        final BlockDetails blockDetails = new BlockDetails();
        blockDetails.setHash(blockObject.getHash());
        blockDetails.setNumber(Numeric.decodeQuantity(blockObject.getNumber()));
        blockDetails.setTimestamp(Numeric.decodeQuantity(blockObject.getTimestamp()));
        blockDetails.setNodeName(nodeName);

        return blockDetails;
    }

    @Setter
    private class BasicNewHead extends NewHead {
        private String hash;

        private String number;

        private String timestamp;

        @Override
        public String getHash() {
            return hash;
        }

        @Override
        public String getNumber() {
            return number;
        }

        @Override
        public String getTimestamp() {
            return timestamp;
        }
    }
}
