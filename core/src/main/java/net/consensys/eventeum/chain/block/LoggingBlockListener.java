package net.consensys.eventeum.chain.block;

import net.consensys.eventeum.dto.block.BlockDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A block listener that logs the block details.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
public class LoggingBlockListener implements BlockListener {

    private static final Logger logger = LoggerFactory.getLogger(LoggingBlockListener.class);

    @Override
    public void onBlock(BlockDetails blockDetails) {
        logger.info(String.format("New block mined. Hash: %s, Number: %s", blockDetails.getHash(), blockDetails.getNumber()));
    }
}
