package io.keyko.monitoring.agent.core.chain.service.health.strategy;

/**
 * A listener interface that is triggered on node failure and recovery events.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface ReconnectionStrategy {

    /**
     * Triggered when an Ethereum node failure is detected.
     */
    void reconnect();

    /**
     * Triggered when it has been detected that the Ethereum node has recovered after failure.
     */
    void resubscribe();
}
