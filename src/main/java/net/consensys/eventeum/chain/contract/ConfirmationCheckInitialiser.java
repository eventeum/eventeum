package net.consensys.eventeum.chain.contract;

import net.consensys.eventeum.chain.config.EventConfirmationConfig;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.block.EventConfirmationBlockListener;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.integration.broadcast.BlockchainEventBroadcaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A contract event listener that initialises a block listener after being passed an unconfirmed event.
 *
 * This created block listener counts blocks since the event was first fired and broadcasts a CONFIRMED
 * event once the configured number of blocks have passed.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
public class ConfirmationCheckInitialiser implements ContractEventListener {

    private BlockchainService blockchainService;
    private BlockchainEventBroadcaster eventBroadcaster;
    private EventConfirmationConfig eventConfirmationConfig;

    @Autowired
    public ConfirmationCheckInitialiser(BlockchainService blockchainService,
                                        BlockchainEventBroadcaster eventBroadcaster,
                                        EventConfirmationConfig eventConfirmationConfig) {
        this.blockchainService = blockchainService;
        this.eventBroadcaster = eventBroadcaster;
        this.eventConfirmationConfig = eventConfirmationConfig;
    }

    @Override
    public void onEvent(ContractEventDetails eventDetails) {
        if (eventDetails.getStatus() == ContractEventStatus.UNCONFIRMED) {
            blockchainService.addBlockListener(createEventConfirmationBlockListener(eventDetails));
        }
    }

    protected BlockListener createEventConfirmationBlockListener(ContractEventDetails eventDetails) {
        return new EventConfirmationBlockListener(eventDetails, blockchainService,
                eventBroadcaster, eventConfirmationConfig);
    }
}
