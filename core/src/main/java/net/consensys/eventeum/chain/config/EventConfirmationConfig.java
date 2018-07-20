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

    public EventConfirmationConfig(@Value("${broadcaster.event.confirmation.numBlocksToWait}")
                                           BigInteger blocksToWaitForConfirmation,
                                   @Value("${broadcaster.event.confirmation.numBlocksToWaitForMissingTx}")
                                           BigInteger blocksToWaitForMissingTx) {
        this.blocksToWaitForConfirmation = blocksToWaitForConfirmation;
        this.blocksToWaitForMissingTx = blocksToWaitForMissingTx;
    }
}
