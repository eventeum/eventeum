package io.keyko.monitoring.agent.core.chain.service.health;

import io.keyko.monitoring.agent.core.chain.service.health.strategy.ReconnectionStrategy;
import io.keyko.monitoring.agent.core.chain.service.BlockchainException;
import io.keyko.monitoring.agent.core.chain.service.BlockchainService;
import io.keyko.monitoring.agent.core.monitoring.EventeumValueMonitor;
import io.keyko.monitoring.agent.core.service.EventStoreService;
import io.keyko.monitoring.agent.core.service.SubscriptionService;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.websocket.EventeumWebSocketService;
import org.web3j.protocol.websocket.WebSocketClient;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class WebSocketHealthCheckService extends NodeHealthCheckService {

    private WebSocketClient webSocketClient;

    public WebSocketHealthCheckService(Web3jService web3jService,
                                       BlockchainService blockchainService,
                                       ReconnectionStrategy failureListener,
                                       SubscriptionService subscriptionService,
                                       EventeumValueMonitor valueMonitor,
                                       EventStoreService eventStoreService,
                                       Integer syncingThreshold,
                                       ScheduledThreadPoolExecutor taskScheduler,
                                       Long healthCheckPollInterval
    ) {
        super(blockchainService, failureListener, subscriptionService,
                valueMonitor, eventStoreService, syncingThreshold, taskScheduler, healthCheckPollInterval);

        if (web3jService instanceof EventeumWebSocketService) {
            this.webSocketClient = ((EventeumWebSocketService) web3jService).getWebSocketClient();
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
