package net.consensys.eventeum.chain.service.health;

import io.micrometer.core.instrument.MeterRegistry;
import net.consensys.eventeum.chain.service.BlockchainException;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.health.strategy.ReconnectionStrategy;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
import net.consensys.eventeum.service.SubscriptionService;
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
                                       MeterRegistry meterRegistry,
                                       SaveableEventStore dbEventStore,
                                       Integer syncingThreshold,
                                       ScheduledThreadPoolExecutor taskScheduler,
                                       Long healthCheckPollInterval
    ) {
        super(blockchainService, failureListener, subscriptionService, meterRegistry, dbEventStore, syncingThreshold, taskScheduler, healthCheckPollInterval);

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
