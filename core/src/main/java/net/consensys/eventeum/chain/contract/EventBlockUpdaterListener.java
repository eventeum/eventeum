package net.consensys.eventeum.chain.contract;

import net.consensys.eventeum.chain.service.EventBlockManagementService;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A contract event listener that updates the latest block number seen for the event spec.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
public class EventBlockUpdaterListener implements ContractEventListener {

    private EventBlockManagementService blockManagement;

    @Autowired
    public EventBlockUpdaterListener(EventBlockManagementService blockManagement) {
        this.blockManagement = blockManagement;
    }

    @Override
    public void onEvent(ContractEventDetails eventDetails) {
        blockManagement.updateLatestBlock(eventDetails.getEventSpecificationSignature(), eventDetails.getBlockNumber(), eventDetails.getAddress());
    }
}
