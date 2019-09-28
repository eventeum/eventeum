package net.consensys.eventeum.chain.contract;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.config.EventConfirmationConfig;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.block.EventConfirmationBlockListener;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.service.AsyncTaskService;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

/**
 * A contract event listener that initialises a block listener after being passed an unconfirmed event.
 *
 * This created block listener counts blocks since the event was first fired and broadcasts a CONFIRMED
 * event once the configured number of blocks have passed.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
@AllArgsConstructor
@Slf4j
public class ConfirmationCheckInitialiser implements ContractEventListener {

    private ChainServicesContainer chainServicesContainer;
    private BlockchainEventBroadcaster eventBroadcaster;
    private EventConfirmationConfig eventConfirmationConfig;
    private AsyncTaskService asyncTaskService;

    @Override
    public void onEvent(ContractEventDetails eventDetails) {
        if (eventDetails.getStatus() == ContractEventStatus.UNCONFIRMED) {

            final BlockchainService blockchainService = getBlockchainService(eventDetails);

            BigInteger currentBlock = blockchainService.getCurrentBlockNumber();
            BigInteger waitBlocks = eventConfirmationConfig.getBlocksToWaitForConfirmation();
            BigInteger confirmationBlock = eventDetails.getBlockNumber().add(waitBlocks);

            if (currentBlock.compareTo(confirmationBlock) >= 0) {
                eventDetails.setStatus(ContractEventStatus.CONFIRMED);
                eventBroadcaster.broadcastContractEvent(eventDetails);

                return;
            }

            log.info("Registering an EventConfirmationBlockListener for event: {}", eventDetails.getId());

            blockchainService.addBlockListener(createEventConfirmationBlockListener(eventDetails));

        }
    }

    protected BlockListener createEventConfirmationBlockListener(ContractEventDetails eventDetails) {
        return new EventConfirmationBlockListener(eventDetails, getBlockchainService(eventDetails),
                eventBroadcaster, eventConfirmationConfig, asyncTaskService);
    }

    private BlockchainService getBlockchainService(ContractEventDetails eventDetails) {
        return chainServicesContainer.getNodeServices(
                eventDetails.getNodeName()).getBlockchainService();
    }
}
