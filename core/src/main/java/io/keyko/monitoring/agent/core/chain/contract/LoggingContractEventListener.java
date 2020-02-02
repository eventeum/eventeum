package io.keyko.monitoring.agent.core.chain.contract;

import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A contract event listener that logs the contract event details.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
public class LoggingContractEventListener implements ContractEventListener {

    private static final Logger logger = LoggerFactory.getLogger(LoggingContractEventListener.class);

    @Override
    public void onEvent(ContractEventDetails eventDetails) {
        logger.info("Contract event fired: " + eventDetails.getName());
    }
}
