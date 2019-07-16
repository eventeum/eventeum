package net.consensys.eventeum.chain.websocket;

import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.BlockchainException;
import net.consensys.eventeum.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.web3j.protocol.websocket.WebSocketClient;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

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
