package io.keyko.monitoring.agent.core.chain.service.health.strategy;

import lombok.extern.slf4j.Slf4j;
import io.keyko.monitoring.agent.core.chain.service.BlockchainService;
import io.keyko.monitoring.agent.core.service.SubscriptionService;

/**
 * An NodeFailureListener that reconnects the blockchain service and resubscribes to all
 * active event subscriptions on recovery.
 * <p>
 * Note:  All subscriptions are unregistered before being reregistered.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Slf4j
public class HttpReconnectionStrategy extends ResubscribingReconnectionStrategy {

    public HttpReconnectionStrategy(SubscriptionService subscriptionService, BlockchainService blockchainService) {
        super(subscriptionService, blockchainService);
    }

    @Override
    public void reconnect() {
        //Do Nothing
    }
}
