package net.consensys.eventeum.chain.service;

import net.consensys.eventeum.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * An NodeFailureListener that reconnects the blockchain service and resubscribes to all
 * active event subscriptions on recovery.
 *
 * Note:  All subscriptions are unregistered before being reregistered.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
public class ResubscribeNodeFailureListener implements NodeFailureListener {

    private SubscriptionService subscriptionService;
    private BlockchainService blockchainService;

    @Autowired
    public ResubscribeNodeFailureListener(SubscriptionService subscriptionService,
                                          BlockchainService blockchainService) {
        this.subscriptionService = subscriptionService;
        this.blockchainService = blockchainService;
    }

    @Override
    public void onNodeFailure() {
        //Do Nothing
    }

    @Override
    public void onNodeRecovery() {
        blockchainService.reconnect();

        //TODO need to figure out if we need to unregister
        subscriptionService.resubscribeToAllSubscriptions(true);
    }
}
