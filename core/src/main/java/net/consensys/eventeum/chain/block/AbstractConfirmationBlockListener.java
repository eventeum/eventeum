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

import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.TransactionReceipt;
import net.consensys.eventeum.chain.service.strategy.BlockSubscriptionStrategy;
import net.consensys.eventeum.chain.settings.Node;
import net.consensys.eventeum.dto.TransactionBasedDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractConfirmationBlockListener<T extends TransactionBasedDetails> extends SelfUnregisteringBlockListener {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractConfirmationBlockListener.class);

    private T blockchainEvent;
    private BlockchainService blockchainService;
    private BigInteger targetBlock;
    private BigInteger blocksToWaitForMissingTx;
    private BigInteger blocksToWait;

    private AtomicBoolean isInvalidated = new AtomicBoolean(false);
    private BigInteger missingTxBlockLimit;
    private BigInteger numBlocksToWaitBeforeInvalidating;
    private BigInteger currentNumBlocksToWaitBeforeInvalidating;

    public AbstractConfirmationBlockListener(T blockchainEvent,
                                             BlockchainService blockchainService,
                                             BlockSubscriptionStrategy blockSubscription,
                                             Node node) {
        super(blockSubscription);
        this.blockchainEvent = blockchainEvent;
        this.blockchainService = blockchainService;

        final BigInteger currentBlock = blockchainService.getCurrentBlockNumber();
        this.blocksToWait = node.getBlocksToWaitForConfirmation();
        this.targetBlock = currentBlock.add(blocksToWait);
        this.blocksToWaitForMissingTx = node.getBlocksToWaitForMissingTx();
        this.numBlocksToWaitBeforeInvalidating = node.getBlocksToWaitBeforeInvalidating();
    }

    @Override
    public void onBlock(Block block) {
        final TransactionReceipt receipt = blockchainService.getTransactionReceipt(
                blockchainEvent.getTransactionHash());

        if (receipt == null) {
            //Tx has disappeared...we've probably forked
            //Tx should be included in block on new fork soon
            handleMissingTransaction(block);
            return;
        }

        checkEventStatus(block, receipt);
    }


    protected abstract String getEventIdentifier(T blockchainEvent);

    protected abstract void setStatus(T blockchainEvent, String status);

    protected abstract void broadcast(T blockchainEvent);


    protected void checkEventStatus(Block block, TransactionReceipt receipt) {
        if (isOrphaned(receipt)) {
            processInvalidatedEvent(block);
        } else if (block.getNumber().compareTo(targetBlock) >= 0) {
            LOG.debug("Target block reached for event: {}", getEventIdentifier(blockchainEvent));
            broadcastEventConfirmed();
            unregister();
        }
    }

    protected boolean isOrphaned(TransactionReceipt receipt) {
        //If block hash is not as expected, this means that the transaction
        //has been included in a block on a different fork of a longer chain
        //and the original transaction is considered orphaned.
        String orphanReason = null;

        if (!receipt.getBlockHash().equals(blockchainEvent.getBlockHash())) {
            orphanReason = "Expected blockhash " + blockchainEvent.getBlockHash() + ", received " + receipt.getBlockHash();
        }

        if (orphanReason != null) {
            LOG.info("Orphan event detected: " + orphanReason);
            return true;
        }

        return false;
    }

    protected void broadcastEventInvalidated() {
        setStatus(blockchainEvent, "INVALIDATED");
        broadcastEvent(blockchainEvent);
    }

    protected void broadcastEventConfirmed() {
        setStatus(blockchainEvent, "CONFIRMED");
        broadcastEvent(blockchainEvent);
    }

    protected void processInvalidatedEvent(Block block) {
        processInvalidatedEvent(block.getNumber());
    }

    protected void processInvalidatedEvent(BigInteger blockNumber) {
        if (currentNumBlocksToWaitBeforeInvalidating == null) {
            currentNumBlocksToWaitBeforeInvalidating = blockNumber.add(numBlocksToWaitBeforeInvalidating);
        } else if (blockNumber.compareTo(currentNumBlocksToWaitBeforeInvalidating) > 0) {
            unRegisterEventListener();
        }
    }

    protected void handleMissingTransaction(Block block) {
        if (missingTxBlockLimit == null) {
            missingTxBlockLimit = block.getNumber().add(blocksToWaitForMissingTx);
        } else if (block.getNumber().compareTo(missingTxBlockLimit) > 0) {
            unRegisterEventListener();
        }
    }

    protected void broadcastEvent(T event) {
        if (!isInvalidated.get()) {
            LOG.debug(String.format("Sending confirmed event for %s event: %s",
                    event.getClass().getSimpleName(), getEventIdentifier(blockchainEvent)));
            broadcast(event);
        }
    }

    protected void unRegisterEventListener() {
        broadcastEventInvalidated();
        isInvalidated.set(true);
        unregister();
    }
}
