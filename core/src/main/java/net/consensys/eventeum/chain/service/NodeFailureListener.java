package net.consensys.eventeum.chain.service;

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
}
