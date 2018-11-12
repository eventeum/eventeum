package net.consensys.eventeum.chain.service.health;

import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.health.listener.NodeFailureListener;
import org.web3j.protocol.websocket.WebSocketClient;

import java.util.List;

public class WebSocketHealthCheckService extends NodeHealthCheckService {

    private WebSocketClient webSocketClient;

    public WebSocketHealthCheckService(BlockchainService blockchainService,
                                       List<NodeFailureListener> failureListeners,
                                       WebSocketClient webSocketClient) {
        super(blockchainService, failureListeners);
        this.webSocketClient = webSocketClient;
    }

    @Override
    protected boolean isSubscribed() {
        return super.isSubscribed() && webSocketClient.isOpen();
    }
}
