package io.keyko.monitoring.agent.core.chain.block;

import io.keyko.monitoring.agent.core.chain.service.domain.Block;
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
    public void onBlock(Block block) {
        logger.info(String.format("New block mined. Hash: %s, Number: %s", block.getHash(), block.getNumber()));
    }
}
