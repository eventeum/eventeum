package net.consensys.eventeum.chain.websocket;

import lombok.extern.slf4j.Slf4j;
import org.web3j.protocol.websocket.WebSocketClient;

import java.net.URI;

@Slf4j
public class RetryableWebSocketClient extends WebSocketClient {

    private WebSocketReconnectionManager reconnectionManager;

    public RetryableWebSocketClient(URI serverUri,
                                    WebSocketReconnectionManager reconnectionManager) {
        super(serverUri);

        this.reconnectionManager = reconnectionManager;
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (remote || code != 1000) {
            reconnectionManager.reconnect(this);
        } else {
            log.info("Code 1000 close detected");
        }

        super.onClose(code, reason, remote);
    }

    @Override
    public void onError(Exception e) {
        super.onError(e);
    }
}
