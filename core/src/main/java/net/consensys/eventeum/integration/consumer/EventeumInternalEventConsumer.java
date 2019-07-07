package net.consensys.eventeum.integration.consumer;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.EventeumMessage;

/**
 * A consumer for internal Eventeum messages sent from a different instance.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface EventeumInternalEventConsumer {
    void onMessage(EventeumMessage<?> message);
}
