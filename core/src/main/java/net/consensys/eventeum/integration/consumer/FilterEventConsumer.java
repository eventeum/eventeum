package net.consensys.eventeum.integration.consumer;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.EventeumMessage;

/**
 * A consumer for ContractEventFilter messages sent from a different Eventeum instance.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface FilterEventConsumer {
    void onMessage(EventeumMessage<ContractEventFilter> message);
}
