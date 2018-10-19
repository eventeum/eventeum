package net.consensys.eventeum.chain.service.strategy;

import net.consensys.eventeum.chain.service.BlockchainException;
import net.consensys.eventeum.chain.service.domain.TransactionReceipt;
import net.consensys.eventeum.chain.service.domain.wrapper.Web3jTransactionReceipt;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.service.AsyncTaskService;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.websocket.events.NewHead;
import org.web3j.utils.Numeric;
import rx.Subscription;

import java.io.IOException;
import java.math.BigInteger;

public class PubSubBlockSubscriptionStrategy extends AbstractBlockSubscriptionStrategy {

    public PubSubBlockSubscriptionStrategy(Web3j web3j, AsyncTaskService asyncTaskService) {
        super(web3j, asyncTaskService);
    }

    @Override
    public Subscription subscribe() {
        blockSubscription = web3j.newHeadsNotifications().subscribe(newHead -> {
            blockListeners.forEach(listener ->
                    asyncTaskService.execute(() -> listener.onBlock(newHeadToBlockDetails(newHead.getParams().getResult()))));
        });

        return blockSubscription;
    }

    private BlockDetails newHeadToBlockDetails(NewHead newHead) {
//        final EthBlock ethBlock = getEthBlock(Numeric.decodeQuantity(newHead.getNumber()));
//
//        return blockToBlockDetails(ethBlock);
        final BlockDetails blockDetails = new BlockDetails();
        blockDetails.setHash(newHead.getHash());
        blockDetails.setNumber(Numeric.decodeQuantity(newHead.getNumber()));

        return blockDetails;
    }

    private EthBlock getEthBlock(BigInteger blockNumber) {
        final DefaultBlockParameterNumber blockParameterNumber = new DefaultBlockParameterNumber(blockNumber);

        try {
            final EthBlock ethBlock =
                    web3j.ethGetBlockByNumber(blockParameterNumber, false).send();

            return ethBlock;
        } catch (IOException e) {
            throw new BlockchainException(
                    String.format("Unable to obtain block with number: %s", blockNumber.toString()), e);
        }
    }

    private BlockDetails blockToBlockDetails(EthBlock ethBlock) {
        final EthBlock.Block block = ethBlock.getBlock();
        final BlockDetails blockDetails = new BlockDetails();

        blockDetails.setNumber(block.getNumber());
        blockDetails.setHash(block.getHash());

        return blockDetails;
    }

}
