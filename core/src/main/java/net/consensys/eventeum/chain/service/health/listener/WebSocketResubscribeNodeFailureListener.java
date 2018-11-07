package net.consensys.eventeum.chain.service.health.listener;

import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.websocket.WebSocketReconnectionManager;
import net.consensys.eventeum.service.SubscriptionService;
import org.web3j.protocol.websocket.WebSocketClient;

/**
 * An NodeFailureListener that reconnects to the websocket server on failure, and
 * reconnects the blockchain service and resubscribes to all
 * active event subscriptions on recovery.
 *
 * Note:  All subscriptions are unregistered before being reregistered.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Slf4j
public class WebSocketResubscribeNodeFailureListener extends ResubscribeNodeFailureListener {

    private WebSocketReconnectionManager reconnectionManager;
    private WebSocketClient client;

    public WebSocketResubscribeNodeFailureListener(SubscriptionService subscriptionService,
                                                   BlockchainService blockchainService,
                                                   WebSocketReconnectionManager reconnectionManager,
                                                   WebSocketClient client) {
        super(subscriptionService,blockchainService);

        this.reconnectionManager = reconnectionManager;
        this.client = client;
    }

    @Override
    public void onNodeFailure() {
        log.info("Reconnecting web socket because of node failure");
        reconnectionManager.reconnect(client);
    }
}
