package net.consensys.eventeum.chain.service.health;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.health.strategy.ReconnectionStrategy;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
import net.consensys.eventeum.service.SubscriptionService;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A service that constantly polls an ethereum node (getClientVersion) in order to ensure that the node
 * is currently running.  If a failure is detected, each configured NodeFailureListener is invoked.
 * This is also the case when it is detected that a node has recovered after failure.
 *
 * The poll interval can be configured with the ethereum.node.healthcheck.pollInterval property.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Slf4j
public class NodeHealthCheckService {

    private BlockchainService blockchainService;

    private NodeStatus nodeStatus;

    private ReconnectionStrategy reconnectionStrategy;

    private SubscriptionService subscriptionService;


    private boolean initiallySubscribed = false;

    private MeterRegistry meterRegistry;

    private AtomicLong currentBlock;

    private AtomicInteger syncing;

    private SaveableEventStore dbEventStore;

    private Integer syncingThreshold;

    public NodeHealthCheckService(BlockchainService blockchainService,
                                  ReconnectionStrategy reconnectionStrategy,
                                  SubscriptionService subscriptionService,
                                  MeterRegistry meterRegistry,
                                  SaveableEventStore dbEventStore,
                                  Integer syncingThreshold,
                                  ScheduledThreadPoolExecutor taskScheduler,
                                  Long healthCheckPollInterval
    ) {
        this.dbEventStore = dbEventStore;
        this.blockchainService = blockchainService;
        this.reconnectionStrategy = reconnectionStrategy;
        this.subscriptionService = subscriptionService;
        this.syncingThreshold = syncingThreshold;
        nodeStatus = NodeStatus.SUBSCRIBED;
        currentBlock = meterRegistry.gauge( blockchainService.getNodeName() +".currentBlock", Tags.of("chain",blockchainService
                .getNodeName()),new
                AtomicLong(0));
        syncing = meterRegistry.gauge(blockchainService.getNodeName() +".syncing", Tags.of("chain",blockchainService
                .getNodeName()),new
                AtomicInteger(0));

        taskScheduler.scheduleWithFixedDelay(() -> this.checkHealth() ,0, healthCheckPollInterval, TimeUnit.MILLISECONDS);
    }

    public void checkHealth() {
        log.trace("Checking health");

        //Can take a few seconds to subscribe initially so if wait until after
        //first subscription to check health
        if (!isSubscribed() && !initiallySubscribed) {
            log.debug("Not initially subscribed");
            return;
        }

        final NodeStatus statusAtStart = nodeStatus;

        if (isNodeConnected()) {
            log.trace("Node connected");
            if (nodeStatus == NodeStatus.DOWN) {
                log.info("Node {} has come back up.", blockchainService.getNodeName());

                //We've come back up
                doResubscribe();
            } else {
                if (statusAtStart != NodeStatus.SUBSCRIBED || !isSubscribed()) {
                    log.info("Node {} not subscribed", blockchainService.getNodeName());
                    doResubscribe();
                } else {
                    initiallySubscribed = true;
                }
            }

        } else {
            log.error("Node {} is down!!", blockchainService.getNodeName());
            nodeStatus = NodeStatus.DOWN;

            if (statusAtStart != NodeStatus.DOWN) {
                subscriptionService.unsubscribeToAllSubscriptions(blockchainService.getNodeName());
            }

            doReconnect();
        }
    }

    protected boolean isNodeConnected() {
        try {
            currentBlock.set(blockchainService.getCurrentBlockNumber().longValue());
            if(currentBlock.longValue()  <= syncingThreshold + dbEventStore.getLatestBlockForNode(blockchainService.getNodeName()).get().getNumber().longValue() ){
                syncing.set(0);
            }
            else {
                syncing.set(1);
            }
        } catch(Throwable t) {
            log.error("Get latest block failed with exception on node " + blockchainService.getNodeName(), t);

            return false;
        }

        return true;
    }

    protected boolean isSubscribed() {
        return blockchainService.isConnected();
    }

    private void doReconnect() {
        reconnectionStrategy.reconnect();

        if (isNodeConnected()) {
            nodeStatus = NodeStatus.CONNECTED;
            doResubscribe();
        }
    }

    private void doResubscribe() {
        reconnectionStrategy.resubscribe();

        nodeStatus = isSubscribed() ? NodeStatus.SUBSCRIBED : NodeStatus.CONNECTED;
    }

    private enum NodeStatus {
        CONNECTED,
        SUBSCRIBED,
        DOWN
    }

}
