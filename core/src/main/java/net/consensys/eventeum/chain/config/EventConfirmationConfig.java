package net.consensys.eventeum.chain.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

/**
 * Configuration relating to event confirmation.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Data
@Component
public class EventConfirmationConfig {

    //The number of blocks to wait before an event is considered confirmed
    private BigInteger blocksToWaitForConfirmation;

    //The number of blocks to wait for a transaction to be remined if forked,
    //before an event is considered invalidated
    private BigInteger blocksToWaitForMissingTx;

    //The number of blocks to wait to consider an event invalid because the block
    //was an invalid block.
    private BigInteger numBlocksToWaitBeforeInvalidating;

    public EventConfirmationConfig(@Value("${broadcaster.event.confirmation.numBlocksToWait}")
                                           BigInteger blocksToWaitForConfirmation,
                                   @Value("${broadcaster.event.confirmation.numBlocksToWaitForMissingTx}")
                                           BigInteger blocksToWaitForMissingTx,
                                   @Value("${broadcaster.event.confirmation.numBlocksToWaitBeforeInvalidating}")
                                           BigInteger numBlocksToWaitBeforeInvalidating
    ) {
        this.blocksToWaitForConfirmation = blocksToWaitForConfirmation;
        this.blocksToWaitForMissingTx = blocksToWaitForMissingTx;
        this.numBlocksToWaitBeforeInvalidating = numBlocksToWaitBeforeInvalidating;
    }
}
