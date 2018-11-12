package net.consensys.eventeum.chain.service.health.listener;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.service.SubscriptionService;

/**
 * An NodeFailureListener that reconnects the blockchain service and resubscribes to all
 * active event subscriptions on recovery.
 *
 * Note:  All subscriptions are unregistered before being reregistered.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@AllArgsConstructor
@Slf4j
public class ResubscribeNodeFailureListener implements NodeFailureListener {

    private SubscriptionService subscriptionService;
    private BlockchainService blockchainService;

    @Override
    public void onNodeFailure() {
        //Do Nothing
    }

    @Override
    public synchronized void onNodeRecovery() {
        //TODO need to figure out if we need to unregister
        subscriptionService.resubscribeToAllSubscriptions(true);

        blockchainService.reconnect();
    }

    @Override
    public void onNodeSubscribed() {
        log.info("Resubscribed after failure");
    }
}
