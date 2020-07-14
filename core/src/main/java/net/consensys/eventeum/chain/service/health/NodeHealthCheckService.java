/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.consensys.eventeum.chain.service.health;

import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.health.strategy.ReconnectionStrategy;
import net.consensys.eventeum.chain.service.strategy.BlockSubscriptionStrategy;
import net.consensys.eventeum.model.LatestBlock;
import net.consensys.eventeum.monitoring.EventeumValueMonitor;
import net.consensys.eventeum.service.EventStoreService;
import net.consensys.eventeum.service.SubscriptionService;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.math.BigInteger;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A service that constantly polls an ethereum node (getCurrentBlockNumber) in order to ensure that the node
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

    private BlockSubscriptionStrategy blockSubscription;

    private NodeStatus nodeStatus;

    private ReconnectionStrategy reconnectionStrategy;

    private SubscriptionService subscriptionService;

    private AtomicLong currentBlock;

    private AtomicInteger syncing;

    private AtomicInteger nodeStatusGauge;

    private EventStoreService eventStoreService;

    private Integer syncingThreshold;

    private ScheduledThreadPoolExecutor taskScheduler;

    private Long healthCheckPollInterval;

    public NodeHealthCheckService(BlockchainService blockchainService,
                                  BlockSubscriptionStrategy blockSubscription,
                                  ReconnectionStrategy reconnectionStrategy,
                                  SubscriptionService subscriptionService,
                                  EventeumValueMonitor valueMonitor,
                                  EventStoreService eventStoreService,
                                  Integer syncingThreshold,
                                  ScheduledThreadPoolExecutor taskScheduler,
                                  Long healthCheckPollInterval) {
        this.eventStoreService = eventStoreService;
        this.blockchainService = blockchainService;
        this.blockSubscription = blockSubscription;
        this.reconnectionStrategy = reconnectionStrategy;
        this.subscriptionService = subscriptionService;
        this.syncingThreshold = syncingThreshold;
        this.taskScheduler = taskScheduler;
        this.healthCheckPollInterval = healthCheckPollInterval;
        nodeStatus = NodeStatus.SUBSCRIBED;

        currentBlock = valueMonitor.monitor( "currentBlock", blockchainService.getNodeName(), new
                AtomicLong(0));
        nodeStatusGauge = valueMonitor.monitor( "status", blockchainService.getNodeName(), new
                AtomicInteger(NodeStatus.SUBSCRIBED.ordinal()));
        syncing = valueMonitor.monitor("syncing", blockchainService.getNodeName(), new
                AtomicInteger(0));

    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Starting healthcheck scheduler");
        taskScheduler.scheduleWithFixedDelay(() -> this.checkHealth() ,0, healthCheckPollInterval, TimeUnit.MILLISECONDS);
    }

    public void checkHealth() {
        try {
            log.trace("Checking health");

            final NodeStatus statusAtStart = nodeStatus;

            if (isNodeConnected() && (isSubscribed()
                    || subscriptionService.getState() == SubscriptionService.SubscriptionServiceState.SYNCING_EVENTS)) {
                log.trace("Node connected");

                if (nodeStatus == NodeStatus.DOWN) {
                    log.info("Node {} has come back up.", blockchainService.getNodeName());
                }

            } else if (subscriptionService.getState() == SubscriptionService.SubscriptionServiceState.SUBSCRIBED) {
                    log.error("Node {} is down or unsubscribed!!", blockchainService.getNodeName());
                    nodeStatus = NodeStatus.DOWN;

                    if (statusAtStart != NodeStatus.DOWN) {
                        blockSubscription.unsubscribe();
                    }

                    doReconnectAndSubscribe();
            } else if (subscriptionService.getState() == SubscriptionService.SubscriptionServiceState.SYNCING_EVENTS) {
                log.error("Node {} is down!!", blockchainService.getNodeName());
                nodeStatus = NodeStatus.DOWN;

                doReconnect();
            }

            nodeStatusGauge.set(nodeStatus.ordinal());
        } catch (Throwable t) {
            log.error("An error occured during the check health / recovery process...Will retry at next poll", t);
        }
    }

    protected boolean isNodeConnected() {
        try {
            currentBlock.set(blockchainService.getCurrentBlockNumber().longValue());

            if(currentBlock.longValue()  <= syncingThreshold + getLatestBlockForNode().getNumber().longValue() ){
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
        return blockSubscription.isSubscribed();
    }

    private void doReconnect() {
        reconnectionStrategy.reconnect();
    }

    private void doReconnectAndSubscribe() {
        reconnectionStrategy.reconnect();

        if (isNodeConnected()) {
            nodeStatus = NodeStatus.CONNECTED;
            doResubscribe();
        }
    }

    private void doResubscribe() {
        reconnectionStrategy.resubscribe();

        nodeStatus = isSubscribed() ? NodeStatus.CONNECTED : NodeStatus.DOWN;
    }

    private LatestBlock getLatestBlockForNode() {
        return eventStoreService.getLatestBlock(
                blockchainService.getNodeName()).orElseGet(() -> {
                    final LatestBlock latestBlock = new LatestBlock();
                    latestBlock.setNumber(BigInteger.ZERO);
                    return latestBlock;
                });
    }

    private enum NodeStatus {
        CONNECTED,
        SUBSCRIBED,
        DOWN
    }

}
