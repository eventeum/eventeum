package net.consensys.eventeum.chain.web3j;

import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.BlockchainException;
import net.consensys.eventeum.service.AsyncTaskService;
import org.springframework.retry.support.RetryTemplate;
import org.web3j.protocol.websocket.WebSocketClient;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class RetryableWebSocketClient extends WebSocketClient {

    private RetryTemplate retryTemplate;

    private AsyncTaskService asyncTaskService;

    private AtomicBoolean isReconnecting = new AtomicBoolean(false);

    public RetryableWebSocketClient(URI serverUri, RetryTemplate retryTemplate, AsyncTaskService asyncTaskService) {
        super(serverUri);

        this.retryTemplate = retryTemplate;
        this.asyncTaskService = asyncTaskService;
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (!isReconnecting.get() && code != 1000) {
            isReconnecting.set(true);
            asyncTaskService.execute(() -> doReconnect());
        }

        super.onClose(code, reason, remote);
    }

    @Override
    public void onError(Exception e) {
        super.onError(e);
    }

    private void doReconnect() {
        retryTemplate.execute((arg) -> {
            try {
                log.info("Attempting websocket reconnection...");
                if (!reconnectBlocking()) {
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
