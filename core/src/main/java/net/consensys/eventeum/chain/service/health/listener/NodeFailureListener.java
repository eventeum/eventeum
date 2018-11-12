package net.consensys.eventeum.chain.service.health.listener;

/**
 * A listener interface that is triggered on node failure and recovery events.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface NodeFailureListener {

    /**
     * Triggered when an Ethereum node failure is detected.
     */
    void onNodeFailure();

    /**
     * Triggered when it has been detected that the Ethereum node has recovered after failure.
     */
    void onNodeRecovery();

    /**
     * Triggered when it has been detected that block / event subscriptions on the Ethereum are valid.
     */
    void onNodeSubscribed();
}
