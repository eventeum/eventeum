package net.consensys.eventeum.chain.websocket;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.BlockchainException;
import net.consensys.eventeum.service.AsyncTaskService;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.web3j.protocol.websocket.WebSocketClient;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class WebSocketReconnectionManager {

    private RetryTemplate retryTemplate;
    private AsyncTaskService asyncTaskService;

    private AtomicBoolean isReconnecting = new AtomicBoolean(false);

    public WebSocketReconnectionManager(RetryTemplate retryTemplate, AsyncTaskService asyncTaskService) {
        this.retryTemplate = retryTemplate;
        this.asyncTaskService = asyncTaskService;
    }

    public synchronized void reconnect(WebSocketClient client) {
        if (!isReconnecting.get()) {
            isReconnecting.set(true);
            asyncTaskService.execute(() -> doReconnect(client));
        } else {
            log.info("Already in reconnecting mode");
        }
    }

    private void doReconnect(WebSocketClient client) {
        retryTemplate.execute((arg) -> {
            try {
                log.info("Attempting websocket reconnection...");
                if (!client.reconnectBlocking()) {
                    throw new BlockchainException("Reconnect failed!");
                } else {
                    log.info("Websocket reconnected successfully.");
                    isReconnecting.set(false);
                }

                return null;
            } catch (InterruptedException e) {
                throw new BlockchainException("Reconnect interrupted", e);
            }
        });
    }
}
