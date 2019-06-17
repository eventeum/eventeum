package net.consensys.eventeum.chain.service.health;

import net.consensys.eventeum.chain.service.BlockchainException;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.health.strategy.ReconnectionStrategy;
import net.consensys.eventeum.service.SubscriptionService;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.websocket.EventeumWebSocketService;
import org.web3j.protocol.websocket.WebSocketClient;

public class WebSocketHealthCheckService extends NodeHealthCheckService {

    private WebSocketClient webSocketClient;

    public WebSocketHealthCheckService(Web3jService web3jService,
                                       BlockchainService blockchainService,
                                       ReconnectionStrategy failureListener,
                                       SubscriptionService subscriptionService) {
        super(blockchainService, failureListener, subscriptionService);

        if (web3jService instanceof EventeumWebSocketService) {
            this.webSocketClient = ((EventeumWebSocketService)web3jService).getWebSocketClient();
        } else {
            throw new BlockchainException(
                    "Non web socket service passed to WebSocketHealthCheckService");
        }

    }

    @Override
    protected boolean isSubscribed() {
        return super.isSubscribed() && webSocketClient.isOpen();
    }
}
