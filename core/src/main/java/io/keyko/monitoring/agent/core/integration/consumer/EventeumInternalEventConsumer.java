package io.keyko.monitoring.agent.core.integration.consumer;

import org.apache.avro.generic.GenericRecord;

/**
 * A consumer for internal Eventeum messages sent from a different instance.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface EventeumInternalEventConsumer {
    void onMessage(GenericRecord message);
}
