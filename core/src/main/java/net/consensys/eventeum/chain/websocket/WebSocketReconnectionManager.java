package net.consensys.eventeum.chain.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.websocket.WebSocketClient;

@Service
@Slf4j
public class WebSocketReconnectionManager {

    public synchronized void reconnect(WebSocketClient client) {
        log.info("Attempting websocket reconnection...");
        try {
            if (!client.reconnectBlocking()) {
                log.error("Reconnect failed!");
            } else {
                log.info("Websocket reconnected successfully.");
            }
        } catch (InterruptedException e) {
            log.error("Reconnect failed!", e);
        }
    }
}
